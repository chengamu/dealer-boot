package com.bocoo.product.service.impl;

import java.util.List;
import java.util.regex.Pattern;

final class ProductFormulaExpressionNormalizer {
    private static final List<Alias> VARIABLE_ALIASES = List.of(
        new Alias("订单宽(in)", "orderWidthIn"),
        new Alias("订单高(in)", "orderHeightIn"),
        new Alias("订单宽(cm)", "orderWidthCm"),
        new Alias("订单高(cm)", "orderHeightCm"),
        new Alias("订单面积(m²)", "orderAreaM2"),
        new Alias("订单面积m²", "orderAreaM2"),
        new Alias("订单面积m2", "orderAreaM2"),
        new Alias("订单宽in", "orderWidthIn"),
        new Alias("订单高in", "orderHeightIn"),
        new Alias("订单宽cm", "orderWidthCm"),
        new Alias("订单高cm", "orderHeightCm"),
        new Alias("面积m²", "orderAreaM2"),
        new Alias("面积m2", "orderAreaM2"),
        new Alias("订单面积", "orderAreaM2"),
        new Alias("订单宽", "orderWidthIn"),
        new Alias("订单高度", "orderHeightIn"),
        new Alias("订单高", "orderHeightIn"),
        new Alias("宽cm", "orderWidthCm"),
        new Alias("高cm", "orderHeightCm"),
        new Alias("宽", "orderWidthIn"),
        new Alias("高", "orderHeightIn"),
        new Alias("面积", "orderAreaM2"),
        new Alias("店铺", "store"),
        new Alias("面料", "fabric"),
        new Alias("产品类型", "productType"),
        new Alias("配置项值", "optionValue")
    );

    private static final List<Alias> FUNCTION_ALIASES = List.of(
        new Alias("MAX", "max"),
        new Alias("MIN", "min"),
        new Alias("ROUND", "round"),
        new Alias("CEIL", "ceil"),
        new Alias("FLOOR", "floor"),
        new Alias("四舍五入", "round"),
        new Alias("向上取整", "ceil"),
        new Alias("向下取整", "floor")
    );

    private static final List<Alias> MATERIAL_FIELD_ALIASES = List.of(
        new Alias("_物料类型", "_materialType"),
        new Alias("_物料编码", "_materialCode"),
        new Alias("_物料名称", "_materialName"),
        new Alias("_属性分组", "_attributeGroup"),
        new Alias("_类型", "_materialType"),
        new Alias("_编码", "_materialCode"),
        new Alias("_名称", "_materialName"),
        new Alias("_名字", "_materialName"),
        new Alias("_厚度", "_THICKNESS")
    );

    private ProductFormulaExpressionNormalizer() {
    }

    static String normalizeFormulaExpression(String input) {
        return normalizeBase(input);
    }

    static String normalizeConditionExpression(String input) {
        return normalizeOutsideQuotes(input, ProductFormulaExpressionNormalizer::normalizeConditionSegment);
    }

    private static String normalizeBase(String input) {
        return normalizeOutsideQuotes(input, ProductFormulaExpressionNormalizer::normalizeBaseSegment);
    }

    private static String normalizeConditionSegment(String segment) {
        return normalizeBaseSegment(segment)
            .replaceAll("并且|且", "&&")
            .replaceAll("或者|或", "||")
            .replaceAll("(?<![!<>=])=(?![=])", "==");
    }

    private static String normalizeBaseSegment(String segment) {
        String expression = segment
            .replace('×', '*')
            .replace('÷', '/')
            .replace('（', '(')
            .replace('）', ')')
            .replace('，', ',');
        expression = replaceAliases(expression, VARIABLE_ALIASES);
        expression = replaceAliases(expression, FUNCTION_ALIASES);
        return replaceAliases(expression, MATERIAL_FIELD_ALIASES);
    }

    private static String normalizeOutsideQuotes(String input, SegmentNormalizer normalizer) {
        String expression = String.valueOf(input == null ? "" : input).trim();
        StringBuilder result = new StringBuilder();
        StringBuilder segment = new StringBuilder();
        char quote = 0;
        boolean escaped = false;

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (quote != 0) {
                result.append(ch);
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == quote) {
                    quote = 0;
                }
                continue;
            }

            if (ch == '"' || ch == '\'') {
                result.append(normalizer.normalize(segment.toString()));
                segment.setLength(0);
                quote = ch;
                result.append(ch);
                continue;
            }

            segment.append(ch);
        }

        result.append(normalizer.normalize(segment.toString()));
        return result.toString();
    }

    private static String replaceAliases(String expression, List<Alias> aliases) {
        String result = expression;
        for (Alias alias : aliases) {
            result = result.replaceAll(Pattern.quote(alias.from()), alias.to());
        }
        return result;
    }

    private record Alias(String from, String to) {
    }

    @FunctionalInterface
    private interface SegmentNormalizer {
        String normalize(String segment);
    }
}
