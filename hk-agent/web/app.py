from __future__ import annotations

from pathlib import Path
from typing import Any
import json

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import FileResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from pydantic import ValidationError

from agents import create_registry
from agents.fabric_cutting.config import (
    load_cutting_config,
    order_type_label,
    product_label,
    product_rule,
    save_cutting_config,
    wrap_mode_rule,
)
from agents.fabric_cutting.models import BusinessRules, CuttingRequest, Material
from agents.fabric_cutting.parser import parse_excel_bytes
from agents.fabric_cutting.reports import list_reports, load_report
from agents.fabric_cutting.service import calculate_cutting
from infra.app_logger import log_event, text_hash


STATIC_DIR = Path(__file__).parent / "static"


def create_app() -> FastAPI:
    app = FastAPI(title="HK Agent", version="0.1.0")
    registry = create_registry()
    app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")
    app.mount("/assets", StaticFiles(directory=STATIC_DIR / "assets"), name="assets")

    @app.get("/api/agents")
    async def list_agents() -> dict[str, list[str]]:
        return {"agents": registry.list_agent_ids()}

    @app.get("/api/fabric-cutting/config")
    async def get_fabric_cutting_config() -> dict[str, Any]:
        return load_cutting_config()

    @app.put("/api/fabric-cutting/config")
    async def put_fabric_cutting_config(payload: dict[str, Any]) -> dict[str, Any]:
        return save_cutting_config(payload)

    @app.post("/api/agents/{agent_id}/run")
    async def run_agent(agent_id: str, payload: dict[str, Any]) -> dict[str, Any]:
        log_event(
            "agent_run_start",
            agent_id=agent_id,
            use_llm=payload.get("use_llm", True),
            input_hash=text_hash(str(payload.get("text") or payload.get("message") or "")),
        )
        try:
            agent = registry.get(agent_id)
        except KeyError as exc:
            log_event("agent_run_error", agent_id=agent_id, error="agent_not_found")
            raise HTTPException(status_code=404, detail=str(exc)) from exc
        result = await agent.run(payload)
        log_event(
            "agent_run_done",
            agent_id=agent_id,
            status=result.get("status"),
            report_id=(result.get("result") or {}).get("report_id") if isinstance(result.get("result"), dict) else None,
            missing_fields=result.get("missing_fields"),
        )
        return result

    @app.post("/api/agents/{agent_id}/stream")
    async def stream_agent(agent_id: str, payload: dict[str, Any]) -> StreamingResponse:
        request_hash = text_hash(str(payload.get("text") or payload.get("message") or ""))
        log_event(
            "agent_stream_start",
            agent_id=agent_id,
            use_llm=payload.get("use_llm", True),
            input_hash=request_hash,
        )
        try:
            agent = registry.get(agent_id)
        except KeyError as exc:
            log_event("agent_stream_error", agent_id=agent_id, input_hash=request_hash, error="agent_not_found")
            raise HTTPException(status_code=404, detail=str(exc)) from exc

        async def events():
            yield _sse("status", {"message": "收到清单，开始整理输入。"})
            if payload.get("use_llm", True):
                yield _sse("status", {"message": "正在调用模型整理裁片、门幅和业务规则。"})
            else:
                yield _sse("status", {"message": "正在使用本地解析整理裁片清单。"})
            yield _sse("status", {"message": "整理完成后将进入排料计算。"})
            result = await agent.run(payload)
            if result.get("status") == "need_clarification":
                log_event(
                    "agent_stream_clarification",
                    agent_id=agent_id,
                    input_hash=request_hash,
                    missing_fields=result.get("missing_fields", []),
                )
                yield _sse(
                    "clarification",
                    {
                        "message": result.get("message", "请补充缺失信息。"),
                        "missing_fields": result.get("missing_fields", []),
                    },
                )
            elif result.get("status") == "ok":
                report_id = result["result"].get("report_id")
                log_event(
                    "agent_stream_done",
                    agent_id=agent_id,
                    input_hash=request_hash,
                    report_id=report_id,
                    recommended_material_width=result["result"].get("recommended_material_width"),
                )
                yield _sse(
                    "done",
                    {
                        "message": "工艺指导卡已生成。",
                        "report_id": report_id,
                        "report_url": f"/reports/{report_id}",
                        "summary": result["result"].get("summary", ""),
                    },
                )
            else:
                log_event(
                    "agent_stream_error",
                    agent_id=agent_id,
                    input_hash=request_hash,
                    error=result.get("error", "生成失败"),
                )
                yield _sse("error", {"message": result.get("error", "生成失败")})

        return StreamingResponse(events(), media_type="text/event-stream")

    @app.post("/api/agents/{agent_id}/chat/stream")
    async def chat_stream_agent(agent_id: str, payload: dict[str, Any]) -> StreamingResponse:
        request_hash = text_hash(str(payload.get("message") or payload.get("text") or ""))
        log_event("agent_chat_stream_start", agent_id=agent_id, action=payload.get("action", "submit"), input_hash=request_hash)
        try:
            agent = registry.get(agent_id)
        except KeyError as exc:
            log_event("agent_chat_stream_error", agent_id=agent_id, input_hash=request_hash, error="agent_not_found")
            raise HTTPException(status_code=404, detail=str(exc)) from exc

        async def events():
            yield _sse("run_started", {"agent_id": agent_id, "thread_id": payload.get("thread_id")})
            yield _sse("assistant_delta", {"text": "收到，我先整理清单。\n"})
            yield _sse("activity_step", {"id": "receive", "label": "接收输入", "status": "done"})

            if payload.get("action") == "confirm":
                async for item in _stream_confirmed_calculation(agent, payload, agent_id, request_hash):
                    yield item
                return

            async for item in _stream_preview(agent, payload, agent_id, request_hash):
                yield item

        return StreamingResponse(events(), media_type="text/event-stream")

    @app.post("/api/fabric-cutting/calculate")
    async def calculate_fabric_cutting(payload: dict[str, Any]) -> dict[str, Any]:
        log_event("fabric_calculate_start", structured=True)
        try:
            request = CuttingRequest.model_validate(payload)
            result = calculate_cutting(request).model_dump()
            log_event("fabric_calculate_done", report_id=result.get("report_id"))
            return result
        except (ValidationError, ValueError) as exc:
            log_event("fabric_calculate_error", error=type(exc).__name__)
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/fabric-cutting/calculate-excel")
    async def calculate_fabric_cutting_excel(
        file: UploadFile = File(...),
        wrap_mode: str | None = Form(default=None),
        height_extra: float | None = Form(default=None),
        fabric_type: str | None = Form(default=None),
        order_type: str | None = Form(default=None),
        height_multiplier: float | None = Form(default=None),
        materials: str | None = Form(default=None),
        roll_total_length: float | None = Form(default=None),
        roll_head_waste: float = Form(default=0),
        roll_tail_waste: float = Form(default=0),
    ) -> dict[str, Any]:
        content = await file.read()
        log_event("fabric_excel_start", file_size=len(content))
        try:
            request = parse_excel_bytes(content)
            if materials:
                widths = [float(item) for item in materials.split(",") if item.strip()]
                request.materials = [Material(id=f"{width:g}m", width=width) for width in widths]
            request.business_rules = BusinessRules(
                fabric_type=fabric_type or request.business_rules.fabric_type,
                order_type=order_type or request.business_rules.order_type,
                wrap_mode=wrap_mode,
                height_extra=height_extra,
                height_multiplier=height_multiplier,
                roll_total_length=roll_total_length,
                roll_head_waste=roll_head_waste,
                roll_tail_waste=roll_tail_waste,
            )
            result = calculate_cutting(request).model_dump()
            log_event("fabric_excel_done", report_id=result.get("report_id"))
            return result
        except Exception as exc:
            log_event("fabric_excel_error", error=type(exc).__name__)
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.get("/reports/{report_id}")
    async def report_page(report_id: str) -> FileResponse:
        return FileResponse(STATIC_DIR / "report.html", headers={"Cache-Control": "no-store"})

    @app.get("/api/reports/{report_id}")
    async def report_data(report_id: str) -> dict[str, Any]:
        try:
            return load_report(report_id)
        except FileNotFoundError as exc:
            raise HTTPException(status_code=404, detail="Report not found") from exc

    @app.get("/api/reports")
    async def reports(limit: int = 50) -> dict[str, Any]:
        return {"reports": list_reports(limit=max(1, min(limit, 200)))}

    @app.get("/")
    async def home() -> FileResponse:
        return FileResponse(STATIC_DIR / "index.html", headers={"Cache-Control": "no-store"})

    return app


app = create_app()


def _sse(event: str, payload: dict[str, Any]) -> str:
    return f"event: {event}\ndata: {json.dumps(payload, ensure_ascii=False)}\n\n"


async def _stream_preview(agent: Any, payload: dict[str, Any], agent_id: str, request_hash: str | None):
    user_text = str(payload.get("message") or payload.get("text") or "")
    config = load_cutting_config()
    if not user_text.strip():
        yield _sse("clarification", _clarification_payload("请粘贴正确的裁剪信息：原材料门幅、每卷长度、裁片宽高数量和包布方式。", ["输入清单"]))
        yield _sse("run_done", {"status": "need_clarification"})
        return

    llm_client = getattr(agent, "llm_client", None)
    if not llm_client or not llm_client.enabled:
        yield _sse("activity_step", {"id": "model_parse", "label": "模型整理", "status": "error"})
        yield _sse("clarification", _clarification_payload("当前模型还没配置好，我暂时不能整理聊天内容。", ["模型配置"]))
        yield _sse("run_done", {"status": "need_clarification"})
        return

    yield _sse("activity_step", {"id": "model_parse", "label": "模型整理门幅、卷长、包布方式和裁片", "status": "running"})
    yield _sse("assistant_delta", {"text": "正在用模型识别门幅、卷长、包布方式和裁片尺寸。\n"})
    try:
        request_data = await llm_client.extract_cutting_request(user_text)
    except Exception as exc:
        log_event("agent_chat_parse_error", agent_id=agent_id, input_hash=request_hash, error=type(exc).__name__)
        yield _sse("activity_step", {"id": "model_parse", "label": "模型整理", "status": "error"})
        yield _sse("clarification", _clarification_payload("请粘贴正确的裁剪信息：原材料门幅、每卷长度、裁片宽高数量和包布方式。", ["输入清单"]))
        yield _sse("usage", llm_client.last_usage or {})
        yield _sse("run_done", {"status": "need_clarification"})
        return

    yield _sse("activity_step", {"id": "model_parse", "label": "模型整理", "status": "done"})
    yield _sse("usage", llm_client.last_usage or {})
    if not request_data or request_data.get("invalid_input"):
        message = await _clarify(agent, user_text, ["输入格式"], request_data, request_data.get("invalid_reason") if request_data else None)
        yield _sse("assistant_delta", {"text": "这段内容不像完整的裁剪订单。\n"})
        yield _sse("clarification", _clarification_payload(message, ["输入格式"]))
        yield _sse("usage", llm_client.last_usage or {})
        yield _sse("run_done", {"status": "need_clarification"})
        return

    request_data = _apply_payload_overrides(_drop_none(request_data), payload, config)
    yield _sse("activity_step", {"id": "validate", "label": "检查关键信息", "status": "running"})
    try:
        request = CuttingRequest.model_validate(request_data)
    except ValidationError as exc:
        missing = _missing_fields_from_validation(exc)
        message = await _clarify(agent, user_text, missing, request_data, str(exc))
        yield _sse("activity_step", {"id": "validate", "label": "检查关键信息", "status": "blocked"})
        yield _sse("assistant_delta", {"text": "模型整理后的清单还不完整，我先把缺的信息问清楚。\n"})
        yield _sse("clarification", _clarification_payload(message, missing))
        yield _sse("usage", llm_client.last_usage or {})
        yield _sse("run_done", {"status": "need_clarification"})
        return

    missing = _missing_business_fields(request)
    if missing:
        message = await _clarify(agent, user_text, missing, request.model_dump(), None)
        yield _sse("activity_step", {"id": "validate", "label": "检查关键信息", "status": "blocked"})
        yield _sse("assistant_delta", {"text": "清单尺寸我整理好了，还差一点业务参数。\n"})
        yield _sse("clarification", _clarification_payload(message, missing))
        yield _sse("usage", llm_client.last_usage or {})
        yield _sse("run_done", {"status": "need_clarification"})
        return

    yield _sse("activity_step", {"id": "validate", "label": "检查关键信息", "status": "done"})
    yield _sse("assistant_delta", {"text": "我已经整理成本次计算清单，请你确认一下。确认后我再计算用料和损耗。\n"})
    yield _sse("preview", _preview_payload(request, config))
    log_event("agent_chat_preview", agent_id=agent_id, input_hash=request_hash, pieces=len(request.pieces), materials=len(request.materials))
    yield _sse("run_done", {"status": "preview"})


async def _stream_confirmed_calculation(agent: Any, payload: dict[str, Any], agent_id: str, request_hash: str | None):
    config = load_cutting_config()
    yield _sse("assistant_delta", {"text": "收到确认，开始计算排料、卷数和损耗。\n"})
    yield _sse("activity_step", {"id": "calculate", "label": "排料计算", "status": "running"})
    try:
        request_data = _apply_payload_overrides(payload.get("confirmed_request") or {}, payload, config)
        request = CuttingRequest.model_validate(request_data)
        result = calculate_cutting(request).model_dump()
    except Exception as exc:
        log_event("agent_chat_calculate_error", agent_id=agent_id, input_hash=request_hash, error=type(exc).__name__)
        yield _sse("activity_step", {"id": "calculate", "label": "排料计算", "status": "error"})
        yield _sse("error", {"message": f"计算失败：{exc}"})
        yield _sse("run_done", {"status": "error"})
        return

    yield _sse("activity_step", {"id": "calculate", "label": "排料计算", "status": "done"})
    yield _sse("activity_step", {"id": "report", "label": "生成 H5 工艺指导卡", "status": "running"})
    explanation = None
    llm_client = getattr(agent, "llm_client", None)
    if llm_client and llm_client.enabled:
        try:
            explanation = await llm_client.explain_report(result)
            yield _sse("usage", llm_client.last_usage or {})
            result["usage_summary"] = llm_client.last_usage or {}
        except Exception as exc:
            log_event("agent_chat_explain_error", agent_id=agent_id, input_hash=request_hash, error=type(exc).__name__)
    result["llm_explanation"] = explanation or result["summary"]
    result["config_snapshot"] = config
    result["report_summary"] = _build_report_summary(result, config)
    if result.get("report_id"):
        from agents.fabric_cutting.reports import save_report

        save_report(result, result["report_id"])
    yield _sse("activity_step", {"id": "report", "label": "生成 H5 工艺指导卡", "status": "done"})
    yield _sse("assistant_delta", {"text": "工艺指导卡已生成。下面是推荐结果和报告入口。\n"})
    yield _sse("result", _result_payload(result, config))
    log_event("agent_chat_done", agent_id=agent_id, input_hash=request_hash, report_id=result.get("report_id"))
    yield _sse("run_done", {"status": "ok"})


async def _clarify(
    agent: Any,
    user_text: str,
    missing_fields: list[str],
    request_data: dict[str, Any] | None,
    validation_error: str | None,
) -> str:
    builder = getattr(agent, "_build_llm_clarification", None)
    if builder:
        return await builder(
            user_text=user_text,
            missing_fields=missing_fields,
            request_data=request_data,
            validation_error=validation_error,
        )
    return "请粘贴正确的裁剪信息：原材料门幅、每卷长度、裁片宽高数量和包布方式。"


def _missing_business_fields(request: CuttingRequest) -> list[str]:
    missing: list[str] = []
    if request.business_rules.resolved_height_extra() is None:
        missing.append("包布方式")
    if request.business_rules.roll_total_length is None:
        missing.append("每卷原材料长度和前后不可用长度")
    return missing


def _missing_fields_from_validation(exc: ValidationError) -> list[str]:
    missing: list[str] = []
    for error in exc.errors():
        loc = error.get("loc", ())
        first = loc[0] if loc else ""
        if first == "materials":
            missing.append("门幅")
        elif first == "pieces":
            missing.append("裁片清单")
        else:
            missing.append("输入清单")
    return list(dict.fromkeys(missing))


def _clarification_payload(message: str, missing_fields: list[str]) -> dict[str, Any]:
    return {
        "message": message,
        "missing_fields": missing_fields,
        "actions": _actions_for_missing(missing_fields),
    }


def _actions_for_missing(missing_fields: list[str]) -> list[dict[str, str]]:
    actions: list[dict[str, str]] = []
    if "门幅" in missing_fields:
        actions.extend(
            [
                {"label": "门幅 2m", "message": "门幅 2m"},
                {"label": "门幅 3m", "message": "门幅 3m"},
                {"label": "门幅 2m 和 3m", "message": "门幅 2m 和 3m"},
            ]
        )
    if "包布方式" in missing_fields:
        actions.extend(
            [
                {"label": "不包 +20cm", "message": "不包"},
                {"label": "罩壳包布 +35cm", "message": "罩壳包布，下轨不包"},
                {"label": "都包布 +55cm", "message": "罩壳和下轨都包布"},
                {"label": "下轨包布 +35cm", "message": "罩壳不包，下轨包布"},
            ]
        )
    if "每卷原材料长度和前后不可用长度" in missing_fields:
        actions.extend(
            [
                {"label": "50-5-5", "message": "面料可用长度 50-5-5"},
                {"label": "30-2-2", "message": "面料可用长度 30-2-2"},
                {"label": "100-5-5", "message": "面料可用长度 100-5-5"},
            ]
        )
    return actions


def _preview_payload(request: CuttingRequest, config: dict[str, Any]) -> dict[str, Any]:
    height_extra = request.business_rules.resolved_height_extra() or 0
    multiplier = request.business_rules.resolved_height_multiplier()
    order_type = request.business_rules.order_type
    return {
        "request": request.model_dump(),
        "summary": {
            "order_no": request.order_no,
            "fabric_type": product_label(config, request.business_rules.fabric_type),
            "order_type": order_type_label(config, order_type),
            "materials": [f"{item.width:g}m" for item in request.materials],
            "piece_rows": len(request.pieces),
            "piece_quantity": sum(item.quantity for item in request.pieces),
            "height_extra": height_extra,
            "height_multiplier": multiplier,
            "roll_total_length": request.business_rules.roll_total_length,
            "roll_head_waste": request.business_rules.roll_head_waste,
            "roll_tail_waste": request.business_rules.roll_tail_waste,
        },
    }


def _result_payload(result: dict[str, Any], config: dict[str, Any]) -> dict[str, Any]:
    summary = _build_report_summary(result, config)
    return {
        "message": "工艺指导卡已生成。",
        "report_id": result.get("report_id"),
        "report_url": f"/reports/{result.get('report_id')}",
        "order_no": summary.get("order_no"),
        "product": summary.get("product_label"),
        "order_type": summary.get("order_type_label"),
        "summary": result.get("summary", ""),
        "llm_cost": summary.get("llm_cost", 0),
        "recommended_material_width": result.get("recommended_material_width"),
        "plans": result.get("plans", []),
    }


def _apply_payload_overrides(request_data: dict[str, Any], payload: dict[str, Any], config: dict[str, Any]) -> dict[str, Any]:
    request_data = dict(request_data or {})
    defaults = config.get("defaults", {})
    business_rules = {
        **request_data.get("business_rules", {}),
        **payload.get("business_rules", {}),
    }
    product_key = payload.get("product") or payload.get("fabric_type") or business_rules.get("fabric_type") or defaults.get("product")
    order_type = payload.get("order_type") or business_rules.get("order_type") or defaults.get("order_type")
    product = product_rule(config, str(product_key) if product_key else None)
    if product_key:
        business_rules["fabric_type"] = str(product_key)
    if order_type:
        business_rules["order_type"] = str(order_type)
    if product.get("height_multiplier") is not None:
        business_rules["height_multiplier"] = float(product["height_multiplier"])
    if "allow_rotate" not in business_rules and product.get("allow_rotate") is not None:
        business_rules["allow_rotate"] = bool(product.get("allow_rotate"))
    wrap_key = payload.get("wrap_mode") or business_rules.get("wrap_mode")
    wrap = wrap_mode_rule(config, str(wrap_key) if wrap_key else None)
    if wrap_key:
        business_rules["wrap_mode"] = str(wrap_key)
    if payload.get("height_extra") is not None:
        business_rules["height_extra"] = payload["height_extra"]
    elif business_rules.get("height_extra") is None and wrap.get("height_extra") is not None:
        business_rules["height_extra"] = wrap["height_extra"]
    elif business_rules.get("height_extra") is None and product.get("default_height_extra") is not None:
        business_rules["height_extra"] = product["default_height_extra"]
    for key in ["roll_total_length", "roll_head_waste", "roll_tail_waste"]:
        if payload.get(key) is not None:
            business_rules[key] = payload[key]
        elif business_rules.get(key) is None and defaults.get(key) is not None:
            business_rules[key] = defaults[key]
    if payload.get("materials"):
        request_data["materials"] = [{"width": float(item)} if not isinstance(item, dict) else item for item in payload["materials"]]
    elif not request_data.get("materials") and defaults.get("materials"):
        request_data["materials"] = [{"width": float(item)} for item in defaults["materials"]]
    request_data["business_rules"] = business_rules
    order_no = payload.get("order_no") or request_data.get("order_no")
    request_data["order_no"] = str(order_no).strip() if order_no else _auto_order_no()
    return request_data


def _auto_order_no() -> str:
    from datetime import datetime

    return "AUTO-" + datetime.now().strftime("%Y%m%d-%H%M%S")


def _build_report_summary(result: dict[str, Any], config: dict[str, Any]) -> dict[str, Any]:
    request = result.get("request", {})
    business_rules = request.get("business_rules", {})
    recommended_width = result.get("recommended_material_width")
    plans = result.get("plans", [])
    recommended = next((item for item in plans if item.get("material_width") == recommended_width), None)
    if recommended is None:
        recommended = next((item for item in plans if item.get("feasible")), {})
    usage = result.get("usage_summary", {})
    return {
        "order_no": request.get("order_no"),
        "order_title": request.get("order_title"),
        "product": business_rules.get("fabric_type"),
        "product_label": product_label(config, business_rules.get("fabric_type")),
        "order_type": business_rules.get("order_type"),
        "order_type_label": order_type_label(config, business_rules.get("order_type")),
        "recommended_material_width": recommended_width,
        "used_length": recommended.get("used_length"),
        "total_material_length": recommended.get("total_material_length"),
        "roll_count": recommended.get("roll_count"),
        "waste_rate": recommended.get("waste_rate"),
        "llm_cost": usage.get("estimated_cost", 0),
    }


def _drop_none(value: Any) -> Any:
    if isinstance(value, dict):
        return {key: _drop_none(item) for key, item in value.items() if item is not None}
    if isinstance(value, list):
        return [_drop_none(item) for item in value]
    return value
