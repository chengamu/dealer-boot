package com.bocoo.product.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class ProductInchFormatter {
    private static final int DENOMINATOR = 8;

    private ProductInchFormatter() {
    }

    static String format(BigDecimal value) {
        if (value == null) return "-";
        boolean negative = value.signum() < 0;
        BigDecimal absolute = value.abs();
        int whole = absolute.setScale(0, RoundingMode.FLOOR).intValueExact();
        int numerator = absolute.subtract(BigDecimal.valueOf(whole))
            .multiply(BigDecimal.valueOf(DENOMINATOR)).setScale(0, RoundingMode.HALF_UP).intValueExact();
        if (numerator == DENOMINATOR) {
            whole++;
            numerator = 0;
        }
        String sign = negative ? "-" : "";
        if (numerator == 0) return sign + whole;
        int divisor = greatestCommonDivisor(numerator, DENOMINATOR);
        return sign + whole + " " + numerator / divisor + "/" + DENOMINATOR / divisor;
    }

    private static int greatestCommonDivisor(int left, int right) {
        int a = Math.abs(left);
        int b = Math.abs(right);
        while (b != 0) {
            int next = a % b;
            a = b;
            b = next;
        }
        return a == 0 ? 1 : a;
    }
}
