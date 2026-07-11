package com.bocoo.product.service.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

final class ProductFormulaDecimalMath {
    private static final MathContext DIVISION_CONTEXT = MathContext.DECIMAL128;

    private ProductFormulaDecimalMath() {
    }

    static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, DIVISION_CONTEXT);
    }

    static BigDecimal applyFunction(String identifier, List<BigDecimal> args) {
        BigDecimal value = args.get(0);
        return switch (identifier) {
            case "max" -> args.stream().max(BigDecimal::compareTo).orElseThrow();
            case "min" -> args.stream().min(BigDecimal::compareTo).orElseThrow();
            case "round" -> value.setScale(scale(args), RoundingMode.HALF_UP);
            case "ceil" -> value.setScale(scale(args), RoundingMode.CEILING);
            case "floor" -> value.setScale(scale(args), RoundingMode.FLOOR);
            default -> throw new IllegalArgumentException("unsupported function");
        };
    }

    private static int scale(List<BigDecimal> args) {
        return args.size() > 1 ? exactScale(args.get(1)) : 0;
    }

    private static int exactScale(BigDecimal value) {
        int scale = value.intValueExact();
        if (scale < 0 || scale > 12) {
            throw new IllegalArgumentException("invalid function scale");
        }
        return scale;
    }
}
