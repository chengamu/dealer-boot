from __future__ import annotations

import json
import os
import tempfile
import uuid
from pathlib import Path
from typing import Any

from agents.fabric_cutting.config import load_cutting_config, order_type_label, product_label


REPORT_DIR = Path(os.getenv("HK_AGENT_REPORT_DIR", Path(tempfile.gettempdir()) / "hk_agent_reports"))


def new_report_id() -> str:
    return uuid.uuid4().hex


def save_report(payload: dict[str, Any], report_id: str | None = None) -> str:
    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    report_id = report_id or new_report_id()
    path = REPORT_DIR / f"{report_id}.json"
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    return report_id


def load_report(report_id: str) -> dict[str, Any]:
    if not report_id.replace("-", "").isalnum():
        raise FileNotFoundError(report_id)
    path = REPORT_DIR / f"{report_id}.json"
    return json.loads(path.read_text(encoding="utf-8"))


def list_reports(limit: int = 50) -> list[dict[str, Any]]:
    if not REPORT_DIR.exists():
        return []
    config = load_cutting_config()
    items: list[dict[str, Any]] = []
    for path in sorted(REPORT_DIR.glob("*.json"), key=lambda item: item.stat().st_mtime, reverse=True):
        try:
            payload = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            continue
        report_id = str(payload.get("report_id") or path.stem)
        request = payload.get("request", {})
        business_rules = request.get("business_rules", {})
        summary = payload.get("report_summary") or {}
        product = summary.get("product") or business_rules.get("fabric_type")
        order_type = summary.get("order_type") or business_rules.get("order_type")
        items.append(
            {
                "id": report_id,
                "url": f"/reports/{report_id}",
                "order_no": request.get("order_no") or summary.get("order_no") or "-",
                "title": request.get("order_title") or "布料裁剪工艺指导卡",
                "product": summary.get("product_label") or product_label(config, product),
                "order_type": summary.get("order_type_label") or order_type_label(config, order_type),
                "recommended_material_width": summary.get("recommended_material_width")
                or payload.get("recommended_material_width"),
                "used_length": summary.get("used_length"),
                "total_material_length": summary.get("total_material_length"),
                "roll_count": summary.get("roll_count"),
                "waste_rate": summary.get("waste_rate"),
                "llm_cost": summary.get("llm_cost") or payload.get("usage_summary", {}).get("estimated_cost", 0),
                "created_at": _format_mtime(path),
            }
        )
        if len(items) >= limit:
            break
    return items


def _format_mtime(path: Path) -> str:
    from datetime import datetime

    return datetime.fromtimestamp(path.stat().st_mtime).strftime("%Y-%m-%d %H:%M")
