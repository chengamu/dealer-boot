package com.bocoo.product.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFormulaExpressionValidatorTest {

    @Test
    void formulaAllowsArithmeticOrderVariables() {
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidth * 12 - 2.0")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("(orderWidth + orderHeight) / 2")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderArea / 100")).isTrue();
    }

    @Test
    void formulaRejectsUnsupportedSyntaxAndUnknownVariables() {
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("if(orderWidth > 12, 2, 1)")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("Math.max(orderWidth, 1)")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidth.value")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidth ? 2 : 1")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("fabric")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("-1")).isFalse();
    }

    @Test
    void conditionMustEvaluateToBoolean() {
        assertThat(ProductFormulaExpressionValidator.isConditionValid("DEFAULT")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidth > 12")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("fabric == \"XLF241801\"")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidth >= 12 && fabric == \"XLF241801\"")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("option_SYSTEM == \"OPTION_VALUE\"")).isTrue();

        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidth")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("1")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("orderWidth + 1")).isFalse();
    }
}
