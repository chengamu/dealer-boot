package com.bocoo.product.service.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFormulaExpressionValidatorTest {

    @Test
    void formulaAllowsArithmeticOrderVariables() {
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidthCm * 12 - 2.0")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("(orderWidthIn + orderHeightIn) / 2")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderAreaM2 / 100")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("orderWidthCm - var_ALU_DEDUCT")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("ceil(orderWidthCm / 10.5)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("floor(orderHeightCm / 10)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("round(orderAreaM2 / 100, 2)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("ceil(orderWidthCm / 10.5, 2)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("floor(orderHeightCm / 10, 2)")).isTrue();
    }

    @Test
    void formulaNormalizesChineseVariablesAndFunctions() {
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("订单宽(cm) * 12 - 2.0")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("四舍五入(订单面积(m²) / 100, 2)")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isFormulaValid("向上取整(订单宽（cm） / 10.5，2)")).isTrue();

        Double result = ProductFormulaExpressionValidator.evaluateFormula("向下取整(订单高(cm) / 10, 2)", sampleContext());
        assertThat(result).isEqualTo(5.08D);
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

    @Test
    void conditionNormalizesChineseOperatorsAndVariables() {
        assertThat(ProductFormulaExpressionValidator.isConditionValid("订单宽(in) >= 12 并且 面料 = \"XLF241801\"")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("产品类型 = \"CUSTOM_CURTAIN\" 或者 配置项值 = \"MOTOR\"")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("订单宽(in) > 10 且 订单高(in) < 30")).isTrue();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("订单宽(in) >= 12 || unknownVariable == 1")).isFalse();
        assertThat(ProductFormulaExpressionValidator.isConditionValid("订单宽(in) < 12 && unknownVariable == 1")).isFalse();

        assertThat(ProductFormulaExpressionValidator.evaluateCondition("订单宽(in) >= 12 并且 面料 = \"XLF241801\"", sampleContext())).isTrue();
        assertThat(ProductFormulaExpressionValidator.evaluateCondition("订单宽(in) > 20 或者 配置项值 = \"CHAIN\"", sampleContext())).isFalse();
    }

    @Test
    void conditionPreservesQuotedBusinessValues() {
        Map<String, Object> context = Map.of(
            "orderWidthIn", 12D,
            "orderHeightIn", 20D,
            "orderWidthCm", 30.48D,
            "orderHeightCm", 50.8D,
            "orderAreaM2", 0.1548D,
            "fabric", "面料",
            "productType", "CUSTOM_CURTAIN",
            "optionValue", "或者"
        );

        assertThat(ProductFormulaExpressionValidator.evaluateCondition("面料 = \"面料\"", context)).isTrue();
        assertThat(ProductFormulaExpressionValidator.evaluateCondition("配置项值 = \"或者\"", context)).isTrue();
    }

    private Map<String, Object> sampleContext() {
        return Map.of(
            "orderWidthIn", 12D,
            "orderHeightIn", 20D,
            "orderWidthCm", 30.48D,
            "orderHeightCm", 50.8D,
            "orderAreaM2", 0.1548D,
            "fabric", "XLF241801",
            "productType", "CUSTOM_CURTAIN",
            "optionValue", "MOTOR"
        );
    }
}
