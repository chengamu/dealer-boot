package com.bocoo.product.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ProductFormulaExpressionParser {
    private final String expression;
    private final Set<String> variables;
    private final boolean conditionMode;
    private final Map<String, Object> context;
    private final boolean sampleContext;
    private int index;

    ProductFormulaExpressionParser(String expression, Set<String> variables, boolean conditionMode,
                                   Map<String, Object> context, boolean sampleContext) {
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
                value = toDecimal(value).add(toDecimal(parseTerm()));
            } else if (match("-")) {
                value = toDecimal(value).subtract(toDecimal(parseTerm()));
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
        String operator = comparisonOperator();
        return operator == null ? left : compare(left, parseExpression(), operator);
    }

    private String comparisonOperator() {
        for (String operator : new String[]{">=", "<=", "==", "!=", ">", "<"}) {
            if (peek(operator)) {
                index += operator.length();
                return operator;
            }
        }
        return null;
    }

    private Object parseTerm() {
        Object value = parseFactor();
        while (true) {
            if (match("*")) {
                value = toDecimal(value).multiply(toDecimal(parseFactor()));
            } else if (match("/")) {
                BigDecimal divisor = toDecimal(parseFactor());
                if (divisor.signum() == 0) {
                    throw new IllegalArgumentException("division by zero");
                }
                value = ProductFormulaDecimalMath.divide(toDecimal(value), divisor);
            } else {
                return value;
            }
        }
    }

    private Object parseFactor() {
        skipSpaces();
        if (match("+")) return toDecimal(parseFactor());
        if (match("-")) return toDecimal(parseFactor()).negate();
        if (match("(")) {
            Object value = conditionMode ? parseOr() : parseExpression();
            if (!match(")")) throw new IllegalArgumentException("missing )");
            return value;
        }
        char ch = current();
        if (ch == '\'' || ch == '"') return parseString(ch);
        if (Character.isDigit(ch) || ch == '.') return parseNumber();
        if (Character.isLetter(ch) || ch == '_') return parseIdentifierValue();
        throw new IllegalArgumentException("invalid token");
    }

    private Object parseIdentifierValue() {
        int start = index;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            if (!Character.isLetterOrDigit(ch) && ch != '_') break;
            index++;
        }
        String identifier = expression.substring(start, index);
        skipSpaces();
        if (peek("(")) return parseFunction(identifier);
        if (peek(".") || peek("[") || peek("?") || peek(":")) {
            throw new IllegalArgumentException("unsupported expression");
        }
        if (!variables.contains(identifier) && !ProductFormulaExpressionValidator.isFormulaVariable(identifier)
            && !(conditionMode && (identifier.startsWith("option_") || identifier.startsWith("material_")))) {
            throw new IllegalArgumentException("unknown variable");
        }
        Object value = context.get(identifier);
        if (value == null && ProductFormulaExpressionValidator.isFormulaVariable(identifier) && sampleContext) {
            return BigDecimal.ONE;
        }
        if (value == null && identifier.startsWith("option_")) return "OPTION_VALUE";
        if (value == null && identifier.startsWith("material_") && sampleContext) return sampleMaterialValue(identifier);
        return value;
    }

    private Object parseFunction(String identifier) {
        if (!match("(")) throw new IllegalArgumentException("missing (");
        List<BigDecimal> args = new ArrayList<>();
        if (!peek(")")) {
            do {
                args.add(toDecimal(parseExpression()));
            } while (match(","));
        }
        if (!match(")")) throw new IllegalArgumentException("missing )");
        if (args.isEmpty()) throw new IllegalArgumentException("invalid function arguments");
        return ProductFormulaDecimalMath.applyFunction(identifier, args);
    }

    private String parseString(char quote) {
        index++;
        StringBuilder builder = new StringBuilder();
        while (index < expression.length() && expression.charAt(index) != quote) {
            builder.append(expression.charAt(index++));
        }
        if (index >= expression.length()) throw new IllegalArgumentException("unterminated string");
        index++;
        return builder.toString();
    }

    private BigDecimal parseNumber() {
        int start = index;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            if (!Character.isDigit(ch) && ch != '.') break;
            index++;
        }
        return new BigDecimal(expression.substring(start, index));
    }

    private boolean compare(Object left, Object right, String operator) {
        if ("==".equals(operator) || "!=".equals(operator)) {
            boolean matches = expressionEquals(left, right);
            return "!=".equals(operator) ? !matches : matches;
        }
        int comparison = toDecimal(left).compareTo(toDecimal(right));
        return switch (operator) {
            case ">" -> comparison > 0;
            case ">=" -> comparison >= 0;
            case "<" -> comparison < 0;
            case "<=" -> comparison <= 0;
            default -> throw new IllegalArgumentException("invalid comparison");
        };
    }

    private boolean expressionEquals(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return toDecimal(left).compareTo(toDecimal(right)) == 0;
        }
        String leftText = left == null ? "" : String.valueOf(left);
        String rightText = right == null ? "" : String.valueOf(right);
        return leftText.equals(rightText)
            || ProductFormulaSimulationSelections.selectedValueMatches(leftText, rightText)
            || ProductFormulaSimulationSelections.selectedValueMatches(rightText, leftText);
    }

    private boolean match(String token) {
        skipSpaces();
        if (!peek(token)) return false;
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
        while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) index++;
    }

    private BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return new BigDecimal(number.toString());
        throw new IllegalArgumentException("number expected");
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) return bool;
        throw new IllegalArgumentException("boolean expected");
    }

    private Object sampleMaterialValue(String identifier) {
        if (identifier.endsWith("_materialType")) return "MOTOR";
        if (identifier.endsWith("_materialCode")) return "XLF241801";
        if (identifier.endsWith("_materialName")) return "XLF241801 Cream";
        if (identifier.endsWith("_attributeGroup")) return "FABRIC";
        return BigDecimal.ONE;
    }
}
