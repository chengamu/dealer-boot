package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.BusinessNumberFormatter;
import com.bocoo.common.core.utils.DecimalContractUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessNumberContractTest {

    @Test
    void validatesInputScaleWithoutRounding() {
        assertTrue(DecimalContractUtils.hasScaleAtMost(new BigDecimal("4.46020"), 4));
        assertFalse(DecimalContractUtils.hasScaleAtMost(new BigDecimal("4.41002"), 4));
        assertTrue(DecimalContractUtils.isNonNegativeWithScale(new BigDecimal("0.325"), 6));
        assertFalse(DecimalContractUtils.isNonNegativeWithScale(new BigDecimal("-0.1"), 6));
    }

    @Test
    void acceptsOnlyExactEighthInches() {
        assertTrue(DecimalContractUtils.isExactFractionStep(new BigDecimal("72.125"), 8));
        assertFalse(DecimalContractUtils.isExactFractionStep(new BigDecimal("2.123"), 8));
        assertEquals("72 1/8 in", BusinessNumberFormatter.inch(new BigDecimal("72.125"), 8));
        assertEquals("2.123 in (!)", BusinessNumberFormatter.inch(new BigDecimal("2.123"), 8));
    }

    @Test
    void formatsQuantityAndMoneyAtTheirOwnBoundaries() {
        assertEquals("1.00", BusinessNumberFormatter.quantity(BigDecimal.ONE, 2));
        assertEquals("0.325", BusinessNumberFormatter.quantity(new BigDecimal("0.325000"), 2));
        assertEquals("15.60", BusinessNumberFormatter.money(new BigDecimal("0.0156").multiply(BigDecimal.valueOf(1000)), 2));
    }
}
