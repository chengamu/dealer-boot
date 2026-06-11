from __future__ import annotations

from agents.fabric_cutting.config import load_cutting_config
from agents.fabric_cutting.maxrects import calculate_plans, choose_recommended_plan
from agents.fabric_cutting.models import CuttingRequest, CuttingResult
from agents.fabric_cutting.reports import new_report_id, save_report


def calculate_cutting(request: CuttingRequest, *, save: bool = True) -> CuttingResult:
    height_extra = request.business_rules.resolved_height_extra()
    if height_extra is None:
        raise ValueError("缺少包布方式或 height_extra，无法确定实际裁剪高度")
    height_multiplier = request.business_rules.resolved_height_multiplier()

    plans = calculate_plans(
        request.materials,
        request.pieces,
        height_extra,
        height_multiplier=height_multiplier,
        allow_rotate=request.business_rules.allow_rotate,
        roll_total_length=request.business_rules.roll_total_length,
        roll_head_waste=request.business_rules.roll_head_waste,
        roll_tail_waste=request.business_rules.roll_tail_waste,
    )
    recommended = choose_recommended_plan(plans)
    result = CuttingResult(
        request=request,
        height_extra=height_extra,
        plans=plans,
        recommended_material_width=recommended.material_width if recommended else None,
        summary=_build_summary(recommended),
    )
    if save:
        report_id = new_report_id()
        result.report_id = report_id
        payload = result.model_dump()
        payload["report_id"] = report_id
        payload["config_snapshot"] = load_cutting_config()
        payload["report_summary"] = _report_summary(payload)
        save_report(payload, report_id)
    return result


def _build_summary(recommended) -> str:
    if recommended is None:
        return "没有可用门幅，存在裁片宽度超过全部原材料门幅。"
    return (
        f"推荐使用 {recommended.material_width:g}m 门幅，"
        f"用料长度 {recommended.used_length:g}m，"
        f"损耗率 {recommended.waste_rate * 100:.2f}% 。"
    )


def _report_summary(payload: dict) -> dict:
    request = payload.get("request", {})
    business_rules = request.get("business_rules", {})
    recommended_width = payload.get("recommended_material_width")
    plans = payload.get("plans", [])
    recommended = next((item for item in plans if item.get("material_width") == recommended_width), None)
    if recommended is None:
        recommended = next((item for item in plans if item.get("feasible")), {})
    return {
        "order_no": request.get("order_no"),
        "order_title": request.get("order_title"),
        "product": business_rules.get("fabric_type"),
        "order_type": business_rules.get("order_type"),
        "recommended_material_width": recommended_width,
        "used_length": recommended.get("used_length"),
        "total_material_length": recommended.get("total_material_length"),
        "roll_count": recommended.get("roll_count"),
        "waste_rate": recommended.get("waste_rate"),
        "llm_cost": payload.get("usage_summary", {}).get("estimated_cost", 0),
    }
