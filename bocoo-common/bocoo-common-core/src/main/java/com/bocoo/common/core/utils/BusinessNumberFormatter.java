package com.bocoo.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BusinessNumberFormatter {

    public static String money(BigDecimal value, int currencyScale) {
        BigDecimal amount = value == null ? BigDecimal.ZERO : value;
        return amount.setScale(currencyScale, RoundingMode.HALF_UP).toPlainString();
    }

    public static String quantity(BigDecimal value, int minimumScale) {
        if (value == null) {
            return "-";
        }
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < minimumScale ? stripped.setScale(minimumScale).toPlainString() : stripped.toPlainString();
    }

    public static String inch(BigDecimal value, int denominator) {
        if (value == null) {
            return "-";
        }
        BigDecimal scaled = value.multiply(BigDecimal.valueOf(denominator));
        if (scaled.stripTrailingZeros().scale() > 0) {
            return value.stripTrailingZeros().toPlainString() + " in (!)";
        }
        long numeratorTotal = scaled.longValueExact();
        long whole = numeratorTotal / denominator;
        int numerator = (int) (numeratorTotal % denominator);
        if (numerator == 0) {
            return whole + " in";
        }
        int divisor = greatestCommonDivisor(numerator, denominator);
        return whole + " " + numerator / divisor + "/" + denominator / divisor + " in";
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
