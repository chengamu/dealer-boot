from __future__ import annotations

from typing import Any, TypedDict

from langgraph.graph import END, StateGraph
from pydantic import ValidationError

from agents.base import Agent
from agents.fabric_cutting.models import CuttingRequest
from agents.fabric_cutting.parser import parse_text_fallback
from agents.fabric_cutting.service import calculate_cutting
from llm.client import LLMClient


class FabricCuttingState(TypedDict, total=False):
    payload: dict[str, Any]
    user_text: str
    request_data: dict[str, Any]
    result: dict[str, Any]
    need_clarification: bool
    clarification: str
    missing_fields: list[str]
    error: str


class FabricCuttingAgent(Agent):
    agent_id = "fabric_cutting"

    def __init__(self, llm_client: LLMClient | None = None) -> None:
        self.llm_client = llm_client or LLMClient()
        self.graph = self._build_graph()

    async def run(self, payload: dict[str, Any]) -> dict[str, Any]:
        state = await self.graph.ainvoke({"payload": payload})
        if "error" in state:
            return {"status": "error", "error": state["error"]}
        if state.get("need_clarification"):
            return {
                "status": "need_clarification",
                "message": state["clarification"],
                "missing_fields": state.get("missing_fields", []),
            }
        return {"status": "ok", "result": state["result"]}

    def _build_graph(self):
        graph = StateGraph(FabricCuttingState)
        graph.add_node("parse_input", self._parse_input)
        graph.add_node("normalize_business_rules", self._normalize_business_rules)
        graph.add_node("ask_missing_info", self._ask_missing_info)
        graph.add_node("calculate_layout", self._calculate_layout)
        graph.add_node("build_report", self._build_report)

        graph.set_entry_point("parse_input")
        graph.add_edge("parse_input", "normalize_business_rules")
        graph.add_conditional_edges(
            "normalize_business_rules",
            self._route_after_normalize,
            {
                "ask_missing_info": "ask_missing_info",
                "calculate_layout": "calculate_layout",
                "end": END,
            },
        )
        graph.add_edge("ask_missing_info", END)
        graph.add_edge("calculate_layout", "build_report")
        graph.add_edge("build_report", END)
        return graph.compile()

    async def _parse_input(self, state: FabricCuttingState) -> FabricCuttingState:
        payload = state["payload"]
        if "request" in payload:
            return {"request_data": payload["request"]}
        if "materials" in payload and "pieces" in payload:
            return {"request_data": payload}

        user_text = str(payload.get("text") or payload.get("message") or "")
        if not user_text.strip():
            return {"error": "缺少 text/message 或结构化 request"}

        request_data = None
        if payload.get("use_llm", True):
            if not self.llm_client.enabled:
                return {
                    "user_text": user_text,
                    "need_clarification": True,
                    "clarification": _build_clarification(["模型配置"]),
                    "missing_fields": ["模型配置"],
                }
            try:
                request_data = await self.llm_client.extract_cutting_request(user_text)
            except Exception as exc:
                message = await self._build_llm_clarification(
                    user_text=user_text,
                    missing_fields=["输入清单"],
                    validation_error=str(exc),
                )
                return {
                    "user_text": user_text,
                    "need_clarification": True,
                    "clarification": message,
                    "missing_fields": ["输入清单"],
                }
            if not request_data:
                message = await self._build_llm_clarification(
                    user_text=user_text,
                    missing_fields=["输入清单"],
                )
                return {
                    "user_text": user_text,
                    "need_clarification": True,
                    "clarification": message,
                    "missing_fields": ["输入清单"],
                }
            if request_data.get("invalid_input"):
                message = await self._build_llm_clarification(
                    user_text=user_text,
                    missing_fields=["输入格式"],
                    request_data=request_data,
                    validation_error=request_data.get("invalid_reason"),
                )
                return {
                    "user_text": user_text,
                    "need_clarification": True,
                    "clarification": message,
                    "missing_fields": ["输入格式"],
                }
        else:
            request_data = parse_text_fallback(user_text)
        request_data = _sanitize_request_data(request_data)
        request_data = self._merge_payload_overrides(request_data, payload)
        return {"user_text": user_text, "request_data": request_data}

    def _merge_payload_overrides(self, request_data: dict[str, Any], payload: dict[str, Any]) -> dict[str, Any]:
        business_rules = {
            **request_data.get("business_rules", {}),
            **payload.get("business_rules", {}),
        }
        if "wrap_mode" in payload and payload["wrap_mode"]:
            business_rules["wrap_mode"] = payload["wrap_mode"]
        if "height_extra" in payload and payload["height_extra"] is not None:
            business_rules["height_extra"] = payload["height_extra"]
        for key in ["roll_total_length", "roll_head_waste", "roll_tail_waste"]:
            if key in payload and payload[key] is not None:
                business_rules[key] = payload[key]
        if business_rules:
            request_data["business_rules"] = business_rules
        if "materials" in payload and payload["materials"]:
            request_data["materials"] = payload["materials"]
        return request_data

    async def _normalize_business_rules(self, state: FabricCuttingState) -> FabricCuttingState:
        if "error" in state or state.get("need_clarification") or "request_data" not in state:
            return state
        try:
            request = CuttingRequest.model_validate(state["request_data"])
        except ValidationError as exc:
            missing = _missing_fields_from_validation(exc)
            message = await self._build_llm_clarification(
                user_text=state.get("user_text", ""),
                missing_fields=missing,
                request_data=state.get("request_data"),
                validation_error=str(exc),
            )
            return {
                "need_clarification": True,
                "clarification": message,
                "missing_fields": missing,
            }

        missing: list[str] = []
        if request.business_rules.resolved_height_extra() is None:
            missing.append("包布方式")
        if request.business_rules.roll_total_length is None:
            missing.append("每卷原材料长度和前后不可用长度")
        if missing:
            message = await self._build_llm_clarification(
                user_text=state.get("user_text", ""),
                missing_fields=missing,
                request_data=request.model_dump(),
            )
            return {
                "request_data": request.model_dump(),
                "need_clarification": True,
                "clarification": message,
                "missing_fields": missing,
            }
        return {"request_data": request.model_dump()}

    def _route_after_normalize(self, state: FabricCuttingState) -> str:
        if "error" in state:
            return "end"
        if state.get("need_clarification"):
            return "ask_missing_info"
        return "calculate_layout"

    async def _ask_missing_info(self, state: FabricCuttingState) -> FabricCuttingState:
        return state

    async def _calculate_layout(self, state: FabricCuttingState) -> FabricCuttingState:
        try:
            request = CuttingRequest.model_validate(state["request_data"])
            result = calculate_cutting(request)
        except Exception as exc:
            return {"error": str(exc)}
        return {"result": result.model_dump()}

    async def _build_report(self, state: FabricCuttingState) -> FabricCuttingState:
        result = state["result"]
        if "llm_parse_error" in state:
            result["llm_parse_error"] = state["llm_parse_error"]
        explanation = await self.llm_client.explain_report(result)
        if explanation:
            result["llm_explanation"] = explanation
        else:
            result["llm_explanation"] = result["summary"]
        return {"result": result}

    async def _build_llm_clarification(
        self,
        *,
        user_text: str,
        missing_fields: list[str],
        request_data: dict[str, Any] | None = None,
        validation_error: str | None = None,
    ) -> str:
        message = await self.llm_client.clarify_missing_info(
            user_text=user_text,
            missing_fields=missing_fields,
            request_data=request_data,
            validation_error=validation_error,
        )
        return message or _build_clarification(missing_fields)


def _build_clarification(missing: list[str]) -> str:
    if "模型配置" in missing:
        return "当前模型还没配置好，我暂时不能整理聊天内容。"
    if "输入清单" in missing:
        return "我还没整理出有效裁剪清单，请补充门幅、每卷长度、裁片宽高数量和包布方式。"
    if "门幅" in missing and "裁片清单" in missing:
        return "我还缺原材料门幅和裁片清单。请补充：门幅 2m/3m，以及每片的宽度、高度、数量。"
    if "门幅" in missing:
        return "我还缺原材料门幅。请告诉我这单可用哪些门幅，例如：2m、3m。"
    if "裁片清单" in missing:
        return "我还缺裁片清单。请补充每片的宽度、高度、数量。"
    if len(missing) == 1 and missing[0] == "包布方式":
        return "请确认这批卷帘的包布方式：不包、罩壳包布、都包布、下轨包布。"
    if len(missing) == 1 and missing[0] == "每卷原材料长度和前后不可用长度":
        return "请补充每卷原材料长度和前后不可用长度，例如：面料可用长度 50-5-5，表示总长50m，前端5m不可用，末端5m不可用。"
    return "我还缺这些信息：" + "、".join(missing) + "。例如：门幅 2m/3m；不包；面料可用长度 50-5-5。"


def _sanitize_request_data(data: dict[str, Any]) -> dict[str, Any]:
    cleaned = _drop_none(data)
    for piece in cleaned.get("pieces", []) or []:
        if "id" in piece:
            piece["id"] = str(piece["id"])
    for material in cleaned.get("materials", []) or []:
        if "id" in material and material["id"] is not None:
            material["id"] = str(material["id"])
    return cleaned


def _drop_none(value: Any) -> Any:
    if isinstance(value, dict):
        return {key: _drop_none(item) for key, item in value.items() if item is not None}
    if isinstance(value, list):
        return [_drop_none(item) for item in value]
    return value


def _missing_fields_from_validation(exc: ValidationError) -> list[str]:
    missing: list[str] = []
    for error in exc.errors():
        loc = error.get("loc", ())
        first = loc[0] if loc else ""
        if first == "materials":
            missing.append("门幅")
        elif first == "pieces":
            missing.append("裁片清单")
        elif first == "business_rules":
            missing.append("业务规则")
        else:
            missing.append("输入清单")
    return list(dict.fromkeys(missing))
