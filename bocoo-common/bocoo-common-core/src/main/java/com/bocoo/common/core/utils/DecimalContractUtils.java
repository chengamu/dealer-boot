package com.bocoo.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Exact validation helpers for user-entered business decimals. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DecimalContractUtils {

    public static boolean hasScaleAtMost(BigDecimal value, int maximumScale) {
        return value == null || value.stripTrailingZeros().scale() <= maximumScale;
    }

    public static boolean isNonNegativeWithScale(BigDecimal value, int maximumScale) {
        return value == null || value.signum() >= 0 && hasScaleAtMost(value, maximumScale);
    }

    public static boolean isExactFractionStep(BigDecimal value, int denominator) {
        if (value == null || denominator <= 0) {
            return false;
        }
        return value.multiply(BigDecimal.valueOf(denominator)).stripTrailingZeros().scale() <= 0;
    }
}
