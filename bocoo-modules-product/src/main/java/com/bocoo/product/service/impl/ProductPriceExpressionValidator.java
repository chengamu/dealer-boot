package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

final class ProductPriceExpressionValidator {
    private static final Set<String> PRICE_VARIABLES = Set.of(
        "unitPrice", "usageQty", "width", "drop", "widthCm", "dropCm", "areaM2", "areaSqft",
        "orderWidthIn", "orderHeightIn", "orderWidthCm", "orderHeightCm", "orderAreaM2"
    );
    private static final Set<String> CONDITION_VARIABLES = Set.of(
        "width", "drop", "widthCm", "dropCm", "areaM2", "areaSqft",
        "orderWidthIn", "orderHeightIn", "orderWidthCm", "orderHeightCm", "orderAreaM2",
        "fabric", "productType", "optionValue"
    );
    private static final Map<String, Object> SAMPLE = Map.ofEntries(
        Map.entry("unitPrice", 20D),
        Map.entry("usageQty", 2D),
        Map.entry("width", 25D),
        Map.entry("drop", 72D),
        Map.entry("widthCm", 63.5D),
        Map.entry("dropCm", 182.88D),
        Map.entry("areaM2", 1.16D),
        Map.entry("areaSqft", 12.5D),
        Map.entry("orderWidthIn", 25D),
        Map.entry("orderHeightIn", 72D),
        Map.entry("orderWidthCm", 63.5D),
        Map.entry("orderHeightCm", 182.88D),
        Map.entry("orderAreaM2", 1.16D),
        Map.entry("fabric", "FABRIC_A"),
        Map.entry("productType", "CUSTOM_CURTAIN"),
        Map.entry("optionValue", "MOTOR")
    );

    private ProductPriceExpressionValidator() {
    }

    static boolean isPriceFormulaValid(String expression) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeFormulaExpression(expression);
        if (StringUtils.isBlank(normalized)) {
            return false;
        }
        try {
            ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, PRICE_VARIABLES, false, SAMPLE, true);
            Object result = parser.parseExpression();
            parser.expectEnd();
            return result instanceof Number number && Double.isFinite(number.doubleValue()) && number.doubleValue() > 0D;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    static boolean isShippingFormulaValid(String expression) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeFormulaExpression(expression);
        return StringUtils.isNotBlank(normalized) && !usesUnitPrice(normalized) && isPriceFormulaValid(normalized);
    }

    static boolean usesUnitPrice(String expression) {
        return ProductFormulaExpressionNormalizer.normalizeFormulaExpression(expression).matches(".*\\bunitPrice\\b.*");
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

    static BigDecimal evaluatePrice(String expression, Map<String, Object> context) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeFormulaExpression(expression);
        ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, PRICE_VARIABLES, false, context, false);
        Object result = parser.parseExpression();
        parser.expectEnd();
        if (!(result instanceof Number number) || !Double.isFinite(number.doubleValue()) || number.doubleValue() < 0D) {
            throw new IllegalArgumentException("invalid price result");
        }
        return BigDecimal.valueOf(number.doubleValue());
    }

    static boolean evaluateCondition(String expression, Map<String, Object> context) {
        String normalized = ProductFormulaExpressionNormalizer.normalizeConditionExpression(expression);
        if ("DEFAULT".equals(normalized)) {
            return true;
        }
        ProductFormulaExpressionParser parser = new ProductFormulaExpressionParser(normalized, CONDITION_VARIABLES, true, context, false);
        Object result = parser.parseOr();
        parser.expectEnd();
        if (!(result instanceof Boolean matched)) {
            throw new IllegalArgumentException("condition must be boolean");
        }
        return matched;
    }
}
