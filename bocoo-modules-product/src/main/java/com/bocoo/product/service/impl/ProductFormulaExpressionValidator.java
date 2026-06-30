package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Small expression validator for formula setup.
 * It intentionally supports only arithmetic formulas and simple boolean conditions.
 */
final class ProductFormulaExpressionValidator {

    private static final Set<String> FORMULA_VARIABLES = Set.of("orderLength", "orderWidth", "orderHeight", "orderWeight", "orderArea");
    private static final Set<String> CONDITION_VARIABLES = Set.of("orderLength", "orderWidth", "orderHeight", "orderWeight", "orderArea", "fabric", "productType", "optionValue");
    private static final Map<String, Object> SAMPLE = Map.of(
        "orderLength", 18D,
        "orderWidth", 12D,
        "orderHeight", 20D,
        "orderWeight", 3D,
        "orderArea", 240D,
        "fabric", "XLF241801",
        "productType", "CUSTOM_CURTAIN",
        "optionValue", "MOTOR"
    );

    private ProductFormulaExpressionValidator() {
    }

    static boolean isFormulaValid(String expression) {
        if (StringUtils.isBlank(expression)) {
            return false;
        }
        try {
            Parser parser = new Parser(expression, FORMULA_VARIABLES, false);
            Object result = parser.parseExpression();
            parser.expectEnd();
            return result instanceof Number number && Double.isFinite(number.doubleValue()) && number.doubleValue() >= 0D;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    static boolean isConditionValid(String expression) {
        if ("DEFAULT".equals(expression)) {
            return true;
        }
        if (StringUtils.isBlank(expression)) {
            return false;
        }
        try {
            Parser parser = new Parser(expression, CONDITION_VARIABLES, true);
            Object result = parser.parseOr();
            parser.expectEnd();
            return result instanceof Boolean;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static final class Parser {
        private final String expression;
        private final Set<String> variables;
        private final boolean conditionMode;
        private int index;

        private Parser(String expression, Set<String> variables, boolean conditionMode) {
            this.expression = expression;
            this.variables = variables;
            this.conditionMode = conditionMode;
        }

        private Object parseOr() {
            Object value = parseAnd();
            while (match("||")) {
                value = toBoolean(value) || toBoolean(parseAnd());
            }
            return value;
        }

        private Object parseAnd() {
            Object value = parseComparison();
            while (match("&&")) {
                value = toBoolean(value) && toBoolean(parseComparison());
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

        private Object parseExpression() {
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
            if (peek("(") || peek(".") || peek("[") || peek("?") || peek(":")) {
                throw new IllegalArgumentException("unsupported expression");
            }
            if (!variables.contains(identifier) && !(conditionMode && identifier.startsWith("option_"))) {
                throw new IllegalArgumentException("unknown variable");
            }
            Object value = SAMPLE.get(identifier);
            if (value == null && identifier.startsWith("option_")) {
                return "OPTION_VALUE";
            }
            return value;
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

        private void expectEnd() {
            skipSpaces();
            if (index != expression.length()) {
                throw new IllegalArgumentException("unexpected tail");
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
    }
}
