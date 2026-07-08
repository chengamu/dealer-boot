package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Small expression validator for formula setup.
 * It intentionally supports only arithmetic formulas and simple boolean conditions.
 */
final class ProductFormulaExpressionValidator {
    private static final Set<String> FORMULA_VARIABLES = Set.of("orderWidthIn", "orderHeightIn", "orderWidthCm", "orderHeightCm", "orderAreaM2");
    private static final Set<String> CONDITION_VARIABLES = Set.of("orderWidthIn", "orderHeightIn", "orderWidthCm", "orderHeightCm", "orderAreaM2", "store", "fabric", "productType", "optionValue");
    private static final Map<String, Object> SAMPLE = Map.of(
        "orderWidthIn", 12D,
        "orderHeightIn", 20D,
        "orderWidthCm", 30.48D,
        "orderHeightCm", 50.8D,
        "orderAreaM2", 0.1548D,
        "store", "SHOP_A",
        "fabric", "XLF241801",
        "productType", "CUSTOM_CURTAIN",
        "optionValue", "MOTOR"
    );

    private ProductFormulaExpressionValidator() {
    }

    static boolean isFormulaValid(String expression) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeFormulaExpression(expression);
        if (StringUtils.isBlank(normalized)) {
            return false;
        }
        try {
            ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, FORMULA_VARIABLES, false, SAMPLE, true);
            Object result = parser.parseExpression();
            parser.expectEnd();
            return result instanceof Number number && Double.isFinite(number.doubleValue()) && number.doubleValue() >= 0D;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    static boolean isConditionValid(String expression) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeConditionExpression(expression);
        if ("DEFAULT".equals(normalized)) {
            return true;
        }
        if (StringUtils.isBlank(normalized)) {
            return false;
        }
        try {
            ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, CONDITION_VARIABLES, true, SAMPLE, true);
            Object result = parser.parseOr();
            parser.expectEnd();
            return result instanceof Boolean;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    static Double evaluateFormula(String expression, Map<String, Object> context) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeFormulaExpression(expression);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, FORMULA_VARIABLES, false, context, false);
        Object result = parser.parseExpression();
        parser.expectEnd();
        if (!(result instanceof Number number) || !Double.isFinite(number.doubleValue()) || number.doubleValue() < 0D) {
            throw new IllegalArgumentException("invalid formula result");
        }
        return number.doubleValue();
    }

    static boolean evaluateCondition(String expression, Map<String, Object> context) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeConditionExpression(expression);
        if ("DEFAULT".equals(normalized)) {
            return true;
        }
        if (StringUtils.isBlank(normalized)) {
            return false;
        }
        ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, CONDITION_VARIABLES, true, context, false);
        Object result = parser.parseOr();
        parser.expectEnd();
        if (!(result instanceof Boolean bool)) {
            throw new IllegalArgumentException("condition must be boolean");
        }
        return bool;
    }

    static boolean isFormulaVariable(String identifier) {
        return identifier != null && identifier.startsWith("var_");
    }

    static String variableName(String variableCode) {
        if (variableCode != null && variableCode.startsWith("var_")) {
            return variableCode;
        }
        return "var_" + variableCode;
    }
}
