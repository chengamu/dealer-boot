from agents.fabric_cutting.models import BusinessRules, CuttingRequest, Material, Piece, WrapMode
from agents.fabric_cutting.parser import parse_text_fallback
from agents.fabric_cutting.service import calculate_cutting


def test_single_piece_fits_material():
    result = calculate_cutting(
        CuttingRequest(
            materials=[Material(width=2)],
            pieces=[Piece(id="1", width=1, height=1, quantity=1)],
            business_rules=BusinessRules(wrap_mode=WrapMode.NO_COVER),
        ),
        save=False,
    )

    plan = result.plans[0]
    assert plan.feasible
    assert len(plan.placements) == 1
    assert plan.placements[0].height == 1.2


def test_too_wide_piece_marks_material_unavailable():
    result = calculate_cutting(
        CuttingRequest(
            materials=[Material(width=2), Material(width=3)],
            pieces=[Piece(id="1", width=2.5, height=1, quantity=1)],
            business_rules=BusinessRules(wrap_mode=WrapMode.NO_COVER),
        ),
        save=False,
    )

    assert not result.plans[0].feasible
    assert result.plans[1].feasible
    assert result.recommended_material_width == 3


def test_quantity_expands_to_multiple_placements():
    result = calculate_cutting(
        CuttingRequest(
            materials=[Material(width=3)],
            pieces=[Piece(id="A", width=1, height=1, quantity=3)],
            business_rules=BusinessRules(height_extra=0),
        ),
        save=False,
    )

    plan = result.plans[0]
    assert plan.feasible
    assert len(plan.placements) == 3


def test_waste_math_is_consistent():
    result = calculate_cutting(
        CuttingRequest(
            materials=[Material(width=2)],
            pieces=[Piece(id="A", width=1, height=1, quantity=2)],
            business_rules=BusinessRules(height_extra=0),
        ),
        save=False,
    )

    plan = result.plans[0]
    assert plan.material_area >= plan.piece_area
    assert round(plan.material_area - plan.piece_area, 4) == plan.waste_area


def test_placements_do_not_overlap():
    result = calculate_cutting(
        CuttingRequest(
            materials=[Material(width=2)],
            pieces=[
                Piece(id="A", width=1.3, height=2, quantity=2),
                Piece(id="B", width=0.8, height=1.5, quantity=3),
                Piece(id="C", width=0.6, height=1.2, quantity=2),
            ],
            business_rules=BusinessRules(height_extra=0.2),
        ),
        save=False,
    )

    placements = result.plans[0].placements
    eps = 1e-6
    for index, current in enumerate(placements):
        for other in placements[index + 1 :]:
            assert (
                current.x + current.width <= other.x + eps
                or other.x + other.width <= current.x + eps
                or current.y + current.height <= other.y + eps
                or other.y + other.height <= current.y + eps
            )


def test_parse_tabular_text_copied_from_excel():
    data = parse_text_fallback(
        "宽度(M)\t高度(M)\t数量（支数）\n"
        "1.395\t2.695\t1\n"
        "0.855\t1.77\t2\n"
    )

    assert len(data["pieces"]) == 2
    assert data["pieces"][0]["width"] == 1.395
    assert data["pieces"][1]["quantity"] == 2
