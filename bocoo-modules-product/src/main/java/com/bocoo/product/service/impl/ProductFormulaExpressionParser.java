package com.bocoo.product.service.impl;

import java.util.Map;
import java.util.Set;

final class ProductFormulaExpressionParser {
    private final String expression;
    private final Set<String> variables;
    private final boolean conditionMode;
    private final Map<String, Object> context;
    private final boolean sampleContext;
    private int index;

    ProductFormulaExpressionParser(String expression, Set<String> variables, boolean conditionMode, Map<String, Object> context, boolean sampleContext) {
        this.expression = expression;
        this.variables = variables;
        this.conditionMode = conditionMode;
        this.context = context == null ? Map.of() : context;
        this.sampleContext = sampleContext;
    }

    Object parseOr() {
        Object value = parseAnd();
        while (match("||")) {
            Object right = parseAnd();
            value = toBoolean(value) || toBoolean(right);
        }
        return value;
    }

    Object parseExpression() {
        Object value = parseTerm();
        while (true) {
            if (match("+")) {
                value = toNumber(value) + toNumber(parseTerm());
            } else if (match("-")) {
                value = toNumber(value) - toNumber(parseTerm());
            } else {
                return value;
            }
        }
    }

    void expectEnd() {
        skipSpaces();
        if (index != expression.length()) {
            throw new IllegalArgumentException("unexpected tail");
        }
    }

    private Object parseAnd() {
        Object value = parseComparison();
        while (match("&&")) {
            Object right = parseComparison();
            value = toBoolean(value) && toBoolean(right);
        }
        return value;
    }

    private Object parseComparison() {
        Object left = parseExpression();
        skipSpaces();
        String operator = null;
        for (String item : new String[]{">=", "<=", "==", "!=", ">", "<"}) {
            if (peek(item)) {
                operator = item;
                index += item.length();
                break;
            }
        }
        if (operator == null) {
            return left;
        }
        Object right = parseExpression();
        return compare(left, right, operator);
    }

    private Object parseTerm() {
        Object value = parseFactor();
        while (true) {
            if (match("*")) {
                value = toNumber(value) * toNumber(parseFactor());
            } else if (match("/")) {
                double divisor = toNumber(parseFactor());
                if (divisor == 0D) {
                    throw new IllegalArgumentException("division by zero");
                }
                value = toNumber(value) / divisor;
            } else {
                return value;
            }
        }
    }

    private Object parseFactor() {
        skipSpaces();
        if (match("+")) {
            return toNumber(parseFactor());
        }
        if (match("-")) {
            return -toNumber(parseFactor());
        }
        if (match("(")) {
            Object value = conditionMode ? parseOr() : parseExpression();
            if (!match(")")) {
                throw new IllegalArgumentException("missing )");
            }
            return value;
        }
        char ch = current();
        if (ch == '\'' || ch == '"') {
            return parseString(ch);
        }
        if (Character.isDigit(ch) || ch == '.') {
            return parseNumber();
        }
        if (Character.isLetter(ch) || ch == '_') {
            return parseIdentifierValue();
        }
        throw new IllegalArgumentException("invalid token");
    }

    private Object parseIdentifierValue() {
        int start = index;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                break;
            }
            index++;
        }
        String identifier = expression.substring(start, index);
        skipSpaces();
        if (peek("(")) {
            return parseFunction(identifier);
        }
        if (peek(".") || peek("[") || peek("?") || peek(":")) {
            throw new IllegalArgumentException("unsupported expression");
        }
        if (!variables.contains(identifier) && !ProductFormulaExpressionValidator.isFormulaVariable(identifier)
            && !(conditionMode && (identifier.startsWith("option_") || identifier.startsWith("material_")))) {
            throw new IllegalArgumentException("unknown variable");
        }
        Object value = context.get(identifier);
        if (value == null && ProductFormulaExpressionValidator.isFormulaVariable(identifier) && sampleContext) {
            return 1D;
        }
        if (value == null && identifier.startsWith("option_")) {
            return "OPTION_VALUE";
        }
        if (value == null && identifier.startsWith("material_") && sampleContext) {
            return sampleMaterialValue(identifier);
        }
        return value;
    }

    private Object parseFunction(String identifier) {
        if (!match("(")) {
            throw new IllegalArgumentException("missing (");
        }
        double value = toNumber(parseExpression());
        double scale = 0D;
        if (match(",")) {
            scale = toNumber(parseExpression());
        }
        if (!match(")")) {
            throw new IllegalArgumentException("missing )");
        }
        return switch (identifier) {
            case "round" -> round(value, scale);
            case "ceil" -> ceil(value, scale);
            case "floor" -> floor(value, scale);
            default -> throw new IllegalArgumentException("unsupported function");
        };
    }

    private double round(double value, double scale) {
        double factor = Math.pow(10D, Math.max(0D, scale));
        return Math.round(value * factor) / factor;
    }

    private double ceil(double value, double scale) {
        double factor = Math.pow(10D, Math.max(0D, scale));
        return Math.ceil(value * factor) / factor;
    }

    private double floor(double value, double scale) {
        double factor = Math.pow(10D, Math.max(0D, scale));
        return Math.floor(value * factor) / factor;
    }

    private String parseString(char quote) {
        index++;
        StringBuilder builder = new StringBuilder();
        while (index < expression.length() && expression.charAt(index) != quote) {
            builder.append(expression.charAt(index++));
        }
        if (index >= expression.length()) {
            throw new IllegalArgumentException("unterminated string");
        }
        index++;
        return builder.toString();
    }

    private Double parseNumber() {
        int start = index;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            if (!Character.isDigit(ch) && ch != '.') {
                break;
            }
            index++;
        }
        return Double.parseDouble(expression.substring(start, index));
    }

    private boolean compare(Object left, Object right, String operator) {
        if ("==".equals(operator)) {
            return String.valueOf(left).equals(String.valueOf(right));
        }
        if ("!=".equals(operator)) {
            return !String.valueOf(left).equals(String.valueOf(right));
        }
        double leftNumber = toNumber(left);
        double rightNumber = toNumber(right);
        return switch (operator) {
            case ">" -> leftNumber > rightNumber;
            case ">=" -> leftNumber >= rightNumber;
            case "<" -> leftNumber < rightNumber;
            case "<=" -> leftNumber <= rightNumber;
            default -> throw new IllegalArgumentException("invalid comparison");
        };
    }

    private boolean match(String token) {
        skipSpaces();
        if (!peek(token)) {
            return false;
        }
        index += token.length();
        return true;
    }

    private boolean peek(String token) {
        return expression.startsWith(token, index);
    }

    private char current() {
        skipSpaces();
        return index >= expression.length() ? '\0' : expression.charAt(index);
    }

    private void skipSpaces() {
        while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
            index++;
        }
    }

    private double toNumber(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("number expected");
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw new IllegalArgumentException("boolean expected");
    }

    private Object sampleMaterialValue(String identifier) {
        if (identifier.endsWith("_materialType")) {
            return "MOTOR";
        }
        if (identifier.endsWith("_materialCode")) {
            return "XLF241801";
        }
        if (identifier.endsWith("_materialName")) {
            return "XLF241801 Cream";
        }
        if (identifier.endsWith("_attributeGroup")) {
            return "FABRIC";
        }
        return 1D;
    }
}
