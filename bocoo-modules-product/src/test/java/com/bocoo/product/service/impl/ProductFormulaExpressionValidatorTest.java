package com.bocoo.product.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFormulaExpressionValidatorTest {

    @Test
    void formulaAllowsArithmeticOrderVariables() {
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidthCm * 12 - 2.0")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("(orderWidthIn + orderLengthIn) / 2")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderAreaM2 / 100")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidthCm - var_ALU_DEDUCT")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("ceil(orderWidthCm / 10.5)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("floor(orderLengthCm / 10)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("round(orderAreaM2 / 100, 2)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("ceil(orderWidthCm / 10.5, 2)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("floor(orderLengthCm / 10, 2)")).isTrue();
    }

    @Test
    void formulaRejectsUnsupportedSyntaxAndUnknownVariables() {
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("if(orderWidthIn > 12, 2, 1)")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("Math.max(orderWidthIn, 1)")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidthIn.value")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidthIn ? 2 : 1")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("fabric")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("-1")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWeight")).isFalse();
    }

    @Test
    void conditionMustEvaluateToBoolean() {
        assertThat(ProductFormulaExpressionValidator.isConditionValid("DEFAULT")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidthIn > 12")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("fabric == \"XLF241801\"")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidthIn >= 12 && fabric == \"XLF241801\"")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("option_SYSTEM == \"OPTION_VALUE\"")).isTrue();

        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidthIn")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("1")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidthIn + 1")).isFalse();
    }
}
