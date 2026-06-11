from __future__ import annotations

from enum import StrEnum
from typing import Literal

from pydantic import BaseModel, Field, field_validator


class WrapMode(StrEnum):
    NO_COVER = "no_cover"
    HEADBOX_COVER = "headbox_cover"
    BOTH_COVER = "both_cover"
    BOTTOM_RAIL_COVER = "bottom_rail_cover"


class FabricType(StrEnum):
    ROLLER_BLIND = "roller_blind"
    ZEBRA_BLIND = "zebra_blind"


class OrderType(StrEnum):
    CUSTOM = "custom"
    BULK = "bulk"


HEIGHT_EXTRAS_M: dict[WrapMode, float] = {
    WrapMode.NO_COVER: 0.20,
    WrapMode.HEADBOX_COVER: 0.35,
    WrapMode.BOTH_COVER: 0.55,
    WrapMode.BOTTOM_RAIL_COVER: 0.35,
}


class Material(BaseModel):
    id: str | None = None
    width: float = Field(gt=0)


class Piece(BaseModel):
    id: str
    width: float = Field(gt=0)
    height: float = Field(gt=0)
    quantity: int = Field(default=1, ge=1)


class BusinessRules(BaseModel):
    unit: Literal["m"] = "m"
    fabric_type: str = FabricType.ROLLER_BLIND
    order_type: str = OrderType.CUSTOM
    allow_rotate: bool = False
    wrap_mode: WrapMode | None = None
    height_extra: float | None = Field(default=None, ge=0)
    height_multiplier: float | None = Field(default=None, gt=0)
    roll_total_length: float | None = Field(default=None, gt=0)
    roll_head_waste: float = Field(default=0, ge=0)
    roll_tail_waste: float = Field(default=0, ge=0)

    def resolved_height_extra(self) -> float | None:
        if self.height_extra is not None:
            return self.height_extra
        if self.wrap_mode is None:
            return None
        return HEIGHT_EXTRAS_M[self.wrap_mode]

    def resolved_height_multiplier(self) -> float:
        if self.height_multiplier is not None:
            return self.height_multiplier
        return 2 if self.fabric_type == FabricType.ZEBRA_BLIND else 1


class CuttingRequest(BaseModel):
    order_no: str | None = None
    materials: list[Material] = Field(default_factory=list)
    pieces: list[Piece] = Field(default_factory=list)
    business_rules: BusinessRules = Field(default_factory=BusinessRules)
    order_title: str = "布料裁剪工艺指导卡"

    @field_validator("materials")
    @classmethod
    def require_materials(cls, value: list[Material]) -> list[Material]:
        if not value:
            raise ValueError("materials is required")
        return value

    @field_validator("pieces")
    @classmethod
    def require_pieces(cls, value: list[Piece]) -> list[Piece]:
        if not value:
            raise ValueError("pieces is required")
        return value


class Placement(BaseModel):
    piece_id: str
    quantity_index: int
    group_no: int = 1
    roll_index: int = 1
    x: float
    y: float
    width: float
    height: float
    original_width: float
    original_height: float


class CuttingPlan(BaseModel):
    material_width: float
    feasible: bool
    used_length: float
    total_material_length: float = 0
    usable_length_per_roll: float | None = None
    roll_total_length: float | None = None
    roll_count: int = 1
    roll_head_waste: float = 0
    roll_tail_waste: float = 0
    material_area: float
    piece_area: float
    waste_area: float
    waste_rate: float
    placements: list[Placement] = Field(default_factory=list)
    reason: str | None = None


class CuttingResult(BaseModel):
    request: CuttingRequest
    height_extra: float
    plans: list[CuttingPlan]
    recommended_material_width: float | None
    summary: str
    report_id: str | None = None
