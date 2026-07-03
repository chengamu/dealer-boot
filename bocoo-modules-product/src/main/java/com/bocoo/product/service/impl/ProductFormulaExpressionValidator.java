package com.bocoo.product.service.impl;
import com.bocoo.common.core.utils.StringUtils;
import java.util.Map;
import java.util.Set;
/**
 * Small expression validator for formula setup.
 * It intentionally supports only arithmetic formulas and simple boolean conditions.
 */
final class ProductFormulaExpressionValidator {
    private static final Set<String> FORMULA_VARIABLES = Set.of("orderWidthIn", "orderLengthIn", "orderWidthCm", "orderLengthCm", "orderAreaM2");
    private static final Set<String> CONDITION_VARIABLES = Set.of("orderWidthIn", "orderLengthIn", "orderWidthCm", "orderLengthCm", "orderAreaM2", "store", "fabric", "productType", "optionValue");
    private static final Map<String, Object> SAMPLE = Map.of(
        "orderWidthIn", 12D,
        "orderLengthIn", 20D,
        "orderWidthCm", 30.48D,
        "orderLengthCm", 50.8D,
        "orderAreaM2", 0.1548D,
        "store", "SHOP_A",
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
            Parser parser = new Parser(expression, FORMULA_VARIABLES, false, SAMPLE);
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
            Parser parser = new Parser(expression, CONDITION_VARIABLES, true, SAMPLE);
            Object result = parser.parseOr();
            parser.expectEnd();
            return result instanceof Boolean;
        } catch (RuntimeException ex) {
            return false;
        }
    }
    static Double evaluateFormula(String expression, Map<String, Object> context) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }
        Parser parser = new Parser(expression, FORMULA_VARIABLES, false, context);
        Object result = parser.parseExpression();
        parser.expectEnd();
        if (!(result instanceof Number number) || !Double.isFinite(number.doubleValue()) || number.doubleValue() < 0D) {
            throw new IllegalArgumentException("invalid formula result");
        }
        return number.doubleValue();
    }

    static boolean isFormulaVariable(String identifier) {
        return identifier != null && identifier.startsWith("var_");
    }

    static String variableName(String variableCode) {
        return "var_" + variableCode;
    }
    static boolean evaluateCondition(String expression, Map<String, Object> context) {
        if ("DEFAULT".equals(expression)) {
            return true;
        }
        if (StringUtils.isBlank(expression)) {
            return false;
        }
        Parser parser = new Parser(expression, CONDITION_VARIABLES, true, context);
        Object result = parser.parseOr();
        parser.expectEnd();
        if (!(result instanceof Boolean bool)) {
            throw new IllegalArgumentException("condition must be boolean");
        }
        return bool;
    }
    private static final class Parser {
        private final String expression;
        private final Set<String> variables;
        private final boolean conditionMode;
        private final Map<String, Object> context;
        private final boolean sampleContext;
        private int index;
        private Parser(String expression, Set<String> variables, boolean conditionMode, Map<String, Object> context) {
            this.expression = expression;
            this.variables = variables;
            this.conditionMode = conditionMode;
            this.context = context == null ? Map.of() : context;
            this.sampleContext = context == SAMPLE;
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
            if (peek("(")) {
                return parseFunction(identifier);
            }
            if (peek(".") || peek("[") || peek("?") || peek(":")) {
                throw new IllegalArgumentException("unsupported expression");
            }
            if (!variables.contains(identifier) && !isFormulaVariable(identifier)
                && !(conditionMode && (identifier.startsWith("option_") || identifier.startsWith("material_")))) {
                throw new IllegalArgumentException("unknown variable");
            }
            Object value = context.get(identifier);
            if (value == null && isFormulaVariable(identifier) && sampleContext) {
                return 1D;
            }
            if (value == null && identifier.startsWith("option_")) {
                return "OPTION_VALUE";
            }
            if (value == null && identifier.startsWith("material_")) {
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

        private String sampleMaterialValue(String identifier) {
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
            return "MATERIAL_VALUE";
        }
    }
}
