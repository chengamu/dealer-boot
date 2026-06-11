from __future__ import annotations

import json
import os
from pathlib import Path
from typing import Any


CONFIG_PATH = Path(
    os.getenv(
        "HK_AGENT_CONFIG_PATH",
        Path(__file__).resolve().parents[2] / "web" / "config" / "fabric_cutting.json",
    )
)


DEFAULT_CONFIG: dict[str, Any] = {
    "order_types": [
        {"key": "custom", "label": "定制", "description": "逐单追溯，保留每件裁片明细"},
        {"key": "bulk", "label": "大货", "description": "同规格批量生产，图纸按规格汇总"},
    ],
    "products": [
        {
            "key": "roller_blind",
            "label": "卷帘",
            "height_multiplier": 1,
            "default_height_extra": 0.2,
            "allow_rotate": False,
        },
        {
            "key": "zebra_blind",
            "label": "斑马帘",
            "height_multiplier": 2,
            "default_height_extra": 0.2,
            "allow_rotate": False,
        },
    ],
    "wrap_modes": [
        {"key": "no_cover", "label": "不包", "height_extra": 0.2},
        {"key": "headbox_cover", "label": "罩壳包布", "height_extra": 0.35},
        {"key": "both_cover", "label": "都包布", "height_extra": 0.55},
        {"key": "bottom_rail_cover", "label": "下轨包布", "height_extra": 0.35},
    ],
    "defaults": {
        "order_type": "custom",
        "product": "roller_blind",
        "materials": [2, 3],
        "roll_total_length": 50,
        "roll_head_waste": 5,
        "roll_tail_waste": 5,
        "currency": "CNY",
        "llm_input_price_per_1m": 1,
        "llm_output_price_per_1m": 2,
    },
    "drawing": {
        "default_collapsed": True,
        "bulk_group_by_spec": True,
        "screen_segment_length": 8,
        "print_segment_length": 5,
    },
}


def load_cutting_config() -> dict[str, Any]:
    if not CONFIG_PATH.exists():
        return DEFAULT_CONFIG.copy()
    try:
        data = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return DEFAULT_CONFIG.copy()
    return _merge(DEFAULT_CONFIG, data)


def save_cutting_config(config: dict[str, Any]) -> dict[str, Any]:
    CONFIG_PATH.parent.mkdir(parents=True, exist_ok=True)
    normalized = _merge(DEFAULT_CONFIG, config)
    CONFIG_PATH.write_text(json.dumps(normalized, ensure_ascii=False, indent=2), encoding="utf-8")
    return normalized


def product_label(config: dict[str, Any], key: str | None) -> str:
    if not key:
        return "-"
    product = next((item for item in config.get("products", []) if item.get("key") == key), None)
    return str(product.get("label") if product else key)


def order_type_label(config: dict[str, Any], key: str | None) -> str:
    if not key:
        return "-"
    order_type = next((item for item in config.get("order_types", []) if item.get("key") == key), None)
    return str(order_type.get("label") if order_type else key)


def product_rule(config: dict[str, Any], key: str | None) -> dict[str, Any]:
    default_key = config.get("defaults", {}).get("product")
    product_key = key or default_key
    return next((item for item in config.get("products", []) if item.get("key") == product_key), {})


def wrap_mode_rule(config: dict[str, Any], key: str | None) -> dict[str, Any]:
    if not key:
        return {}
    return next((item for item in config.get("wrap_modes", []) if item.get("key") == key), {})


def _merge(base: dict[str, Any], override: dict[str, Any]) -> dict[str, Any]:
    merged = dict(base)
    for key, value in override.items():
        if isinstance(value, dict) and isinstance(merged.get(key), dict):
            merged[key] = _merge(merged[key], value)
        else:
            merged[key] = value
    return merged
