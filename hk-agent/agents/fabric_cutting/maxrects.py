from __future__ import annotations

from dataclasses import dataclass, field
from math import ceil

from agents.fabric_cutting.models import CuttingPlan, Material, Piece, Placement


@dataclass(frozen=True)
class _ExpandedPiece:
    piece_id: str
    quantity_index: int
    width: float
    height: float
    original_width: float
    original_height: float


@dataclass
class _Shelf:
    group_no: int
    height: float
    used_width: float = 0
    pieces: list[tuple[_ExpandedPiece, float]] = field(default_factory=list)

    def can_fit(self, piece: _ExpandedPiece, material_width: float) -> bool:
        return self.used_width + piece.width <= material_width + 1e-9

    def add(self, piece: _ExpandedPiece) -> None:
        self.pieces.append((piece, self.used_width))
        self.used_width += piece.width


def calculate_plans(
    materials: list[Material],
    pieces: list[Piece],
    height_extra: float,
    *,
    height_multiplier: float = 1,
    allow_rotate: bool = False,
    precision: float = 0.01,
    roll_total_length: float | None = None,
    roll_head_waste: float = 0,
    roll_tail_waste: float = 0,
) -> list[CuttingPlan]:
    expanded = _expand_pieces(pieces, height_extra, height_multiplier)
    return [
        _calculate_plan_for_material(
            material.width,
            expanded,
            allow_rotate,
            precision,
            roll_total_length,
            roll_head_waste,
            roll_tail_waste,
        )
        for material in materials
    ]


def choose_recommended_plan(plans: list[CuttingPlan]) -> CuttingPlan | None:
    feasible = [plan for plan in plans if plan.feasible]
    if not feasible:
        return None
    return min(feasible, key=lambda plan: (plan.waste_rate, plan.material_area, plan.total_material_length))


def _expand_pieces(
    pieces: list[Piece],
    height_extra: float,
    height_multiplier: float,
) -> list[_ExpandedPiece]:
    expanded: list[_ExpandedPiece] = []
    for piece in pieces:
        for quantity_index in range(1, piece.quantity + 1):
            expanded.append(
                _ExpandedPiece(
                    piece_id=piece.id,
                    quantity_index=quantity_index,
                    width=piece.width,
                    height=(piece.height + height_extra) * height_multiplier,
                    original_width=piece.width,
                    original_height=piece.height,
                )
            )
    return expanded


def _calculate_plan_for_material(
    material_width: float,
    pieces: list[_ExpandedPiece],
    allow_rotate: bool,
    precision: float,
    roll_total_length: float | None,
    roll_head_waste: float,
    roll_tail_waste: float,
) -> CuttingPlan:
    if allow_rotate:
        too_wide = [piece for piece in pieces if piece.width > material_width and piece.height > material_width]
    else:
        too_wide = [piece for piece in pieces if piece.width > material_width]
    piece_area = sum(piece.width * piece.height for piece in pieces)
    if too_wide:
        return CuttingPlan(
            material_width=round(material_width, 4),
            feasible=False,
            used_length=0,
            total_material_length=0,
            material_area=0,
            piece_area=round(piece_area, 4),
            waste_area=0,
            waste_rate=0,
            reason=f"{len(too_wide)} 个裁片宽度超过 {material_width}m 门幅",
        )

    shelves = _pack_horizontal_shelves(material_width, pieces)
    required_length = sum(shelf.height for shelf in shelves)
    usable_length = _resolve_usable_length(roll_total_length, roll_head_waste, roll_tail_waste)
    if usable_length is not None and any(shelf.height > usable_length + 1e-9 for shelf in shelves):
        return CuttingPlan(
            material_width=round(material_width, 4),
            feasible=False,
            used_length=round(_ceil_to_precision(required_length, precision), 4),
            total_material_length=0,
            usable_length_per_roll=round(usable_length, 4),
            roll_total_length=round(roll_total_length or 0, 4) or None,
            material_area=0,
            piece_area=round(piece_area, 4),
            waste_area=0,
            waste_rate=0,
            reason=f"存在单组高度超过每卷可用长度 {usable_length:g}m",
        )

    placements, roll_count = _build_placements(shelves, usable_length)
    used_length = _ceil_to_precision(required_length, precision)
    if roll_total_length:
        total_material_length = roll_count * roll_total_length
    else:
        total_material_length = used_length
    material_area = material_width * total_material_length
    waste_area = max(material_area - piece_area, 0)
    waste_rate = waste_area / material_area if material_area else 0

    return CuttingPlan(
        material_width=round(material_width, 4),
        feasible=True,
        used_length=round(used_length, 4),
        total_material_length=round(total_material_length, 4),
        usable_length_per_roll=round(usable_length, 4) if usable_length is not None else None,
        roll_total_length=round(roll_total_length, 4) if roll_total_length else None,
        roll_count=roll_count,
        roll_head_waste=round(roll_head_waste, 4),
        roll_tail_waste=round(roll_tail_waste, 4),
        material_area=round(material_area, 4),
        piece_area=round(piece_area, 4),
        waste_area=round(waste_area, 4),
        waste_rate=round(waste_rate, 6),
        placements=placements,
    )


def _pack_horizontal_shelves(material_width: float, pieces: list[_ExpandedPiece]) -> list[_Shelf]:
    shelves: list[_Shelf] = []
    ordered = sorted(pieces, key=lambda piece: (piece.height, piece.width), reverse=True)
    for piece in ordered:
        target = next((shelf for shelf in shelves if shelf.can_fit(piece, material_width)), None)
        if target is None:
            target = _Shelf(group_no=len(shelves) + 1, height=piece.height)
            shelves.append(target)
        target.add(piece)
    return shelves


def _build_placements(shelves: list[_Shelf], usable_length: float | None) -> tuple[list[Placement], int]:
    placements: list[Placement] = []
    roll_index = 1
    y = 0.0
    for shelf in shelves:
        if usable_length is not None and y > 0 and y + shelf.height > usable_length + 1e-9:
            roll_index += 1
            y = 0.0
        for piece, x in shelf.pieces:
            placements.append(
                Placement(
                    piece_id=piece.piece_id,
                    quantity_index=piece.quantity_index,
                    group_no=shelf.group_no,
                    roll_index=roll_index,
                    x=round(x, 4),
                    y=round(y, 4),
                    width=round(piece.width, 4),
                    height=round(piece.height, 4),
                    original_width=round(piece.original_width, 4),
                    original_height=round(piece.original_height, 4),
                )
            )
        y += shelf.height
    return placements, roll_index


def _resolve_usable_length(
    roll_total_length: float | None,
    roll_head_waste: float,
    roll_tail_waste: float,
) -> float | None:
    if roll_total_length is None:
        return None
    usable = roll_total_length - roll_head_waste - roll_tail_waste
    if usable <= 0:
        raise ValueError("每卷可用长度必须大于 0")
    return usable


def _ceil_to_precision(value: float, precision: float) -> float:
    return ceil((value - 1e-9) / precision) * precision
