package com.bocoo.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.product.domain.bo.PriceCalculationBo;
import com.bocoo.product.domain.vo.PriceCalculationResultVo;
import com.bocoo.product.domain.vo.PriceRuleItemVo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 价格计算引擎。
 *
 * 当前阶段只执行受控的 JSON 条件匹配和基础公式，避免任意表达式带来的安全与可追溯风险。
 */
@Component
public class PriceCalculationEngine {

    public PriceCalculationResultVo calculate(PriceCalculationBo bo, List<PriceRuleItemVo> ruleItems) {
        PriceCalculationResultVo result = new PriceCalculationResultVo();
        if (bo == null) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.price.request.required");
            return result;
        }
        result.setPricePlanVersionId(bo.getPricePlanVersionId());
        result.setCurrencyCode(bo.getCurrencyCode());
        if (bo.getPricePlanVersionId() == null) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.price.planVersion.required");
            return result;
        }
        if (ruleItems == null || ruleItems.isEmpty()) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.price.rule.missing");
            return result;
        }
        BigDecimal quantity = bo.getQuantity() == null || BigDecimal.ZERO.compareTo(bo.getQuantity()) == 0 ? BigDecimal.ONE : bo.getQuantity();
        BigDecimal baseAmount = BigDecimal.ZERO;
        BigDecimal optionAmount = BigDecimal.ZERO;
        for (PriceRuleItemVo item : ruleItems) {
            if (!matches(bo, item, result)) {
                continue;
            }
            result.getMatchedItems().add(item);
            BigDecimal itemBase = item.getBaseAmount() == null ? BigDecimal.ZERO : item.getBaseAmount();
            BigDecimal basisValue = resolveBasisValue(bo, item, quantity);
            BigDecimal itemUnit = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice().multiply(basisValue);
            if ("OPTION_ADDER".equals(item.getItemType())) {
                optionAmount = optionAmount.add(itemBase).add(itemUnit);
            } else {
                baseAmount = baseAmount.add(itemBase).add(itemUnit);
            }
            Map<String, Object> breakdown = new LinkedHashMap<>();
            breakdown.put("itemCode", item.getItemCode());
            breakdown.put("itemType", item.getItemType());
            breakdown.put("basisValue", basisValue);
            breakdown.put("amount", itemBase.add(itemUnit));
            breakdown.put("matchJson", item.getMatchJson());
            breakdown.put("formulaJson", item.getFormulaJson());
            result.getBreakdown().add(breakdown);
            if (result.getCurrencyCode() == null) {
                result.setCurrencyCode(item.getCurrencyCode());
            }
        }
        if (result.getMatchedItems().isEmpty()) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.price.rule.noMatch");
            return result;
        }
        result.setBaseAmount(baseAmount);
        result.setOptionAmount(optionAmount);
        result.setTotalAmount(baseAmount.add(optionAmount));
        return result;
    }

    private boolean matches(PriceCalculationBo bo, PriceRuleItemVo item, PriceCalculationResultVo result) {
        if (StringUtils.isBlank(item.getMatchJson())) {
            return true;
        }
        Map<String, Object> match;
        try {
            match = JsonUtils.parseObject(item.getMatchJson(), new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (RuntimeException e) {
            result.getWarnings().add("product.price.rule.matchJson.invalid:" + item.getItemCode());
            return false;
        }
        if (match == null || match.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, Object> entry : match.entrySet()) {
            String key = entry.getKey();
            Object expected = entry.getValue();
            if ("selectedOptions".equals(key) && expected instanceof Map<?, ?> expectedMap) {
                if (!matchesNested(bo.getSelectedOptions(), expectedMap)) {
                    return false;
                }
                continue;
            }
            if ("inputValues".equals(key) && expected instanceof Map<?, ?> expectedMap) {
                if (!matchesNested(bo.getInputValues(), expectedMap)) {
                    return false;
                }
                continue;
            }
            if (!matchesValue(resolveInputValue(bo, key), expected)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesNested(Map<String, Object> actualMap, Map<?, ?> expectedMap) {
        for (Map.Entry<?, ?> entry : expectedMap.entrySet()) {
            if (!matchesValue(actualMap == null ? null : actualMap.get(String.valueOf(entry.getKey())), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesValue(Object actual, Object expected) {
        if (expected instanceof Collection<?> expectedValues) {
            return expectedValues.stream().anyMatch(value -> matchesValue(actual, value));
        }
        if (expected instanceof Map<?, ?> expectedMap) {
            return matchesOperator(actual, expectedMap);
        }
        return Objects.equals(String.valueOf(actual), String.valueOf(expected));
    }

    private boolean matchesOperator(Object actual, Map<?, ?> expectedMap) {
        if (expectedMap.containsKey("eq") && !matchesValue(actual, expectedMap.get("eq"))) {
            return false;
        }
        if (expectedMap.containsKey("in") && !matchesValue(actual, expectedMap.get("in"))) {
            return false;
        }
        BigDecimal actualNumber = toBigDecimal(actual);
        if (expectedMap.containsKey("min")) {
            BigDecimal min = toBigDecimal(expectedMap.get("min"));
            if (actualNumber == null || min == null || actualNumber.compareTo(min) < 0) {
                return false;
            }
        }
        if (expectedMap.containsKey("max")) {
            BigDecimal max = toBigDecimal(expectedMap.get("max"));
            if (actualNumber == null || max == null || actualNumber.compareTo(max) > 0) {
                return false;
            }
        }
        return true;
    }

    private Object resolveInputValue(PriceCalculationBo bo, String key) {
        if ("width".equals(key) || "width_cm".equals(key)) {
            return bo.getWidth() == null ? mapValue(bo.getInputValues(), key) : bo.getWidth();
        }
        if ("height".equals(key) || "height_cm".equals(key)) {
            return bo.getHeight() == null ? mapValue(bo.getInputValues(), key) : bo.getHeight();
        }
        if ("quantity".equals(key)) {
            return bo.getQuantity();
        }
        if ("currencyCode".equals(key)) {
            return bo.getCurrencyCode();
        }
        if ("productModelCode".equals(key)) {
            return bo.getProductModelCode();
        }
        if ("salesVariantCode".equals(key)) {
            return bo.getSalesVariantCode();
        }
        Object selected = mapValue(bo.getSelectedOptions(), key);
        return selected == null ? mapValue(bo.getInputValues(), key) : selected;
    }

    private Object mapValue(Map<String, Object> values, String key) {
        return values == null ? null : values.get(key);
    }

    private BigDecimal resolveBasisValue(PriceCalculationBo bo, PriceRuleItemVo item, BigDecimal quantity) {
        Map<String, Object> formula = parseFormula(item.getFormulaJson());
        String basis = formula == null ? null : String.valueOf(formula.getOrDefault("basis", formula.get("chargeBy")));
        BigDecimal divisor = formula == null ? BigDecimal.ONE : defaultBigDecimal(formula.get("divisor"), BigDecimal.ONE);
        BigDecimal value;
        if ("AREA".equals(item.getItemType()) || "AREA".equals(basis)) {
            value = dimension(bo, "width", "width_cm").multiply(dimension(bo, "height", "height_cm")).multiply(quantity);
        } else if ("WIDTH".equals(basis)) {
            value = dimension(bo, "width", "width_cm").multiply(quantity);
        } else if ("HEIGHT".equals(basis)) {
            value = dimension(bo, "height", "height_cm").multiply(quantity);
        } else {
            value = quantity;
        }
        return divisor.compareTo(BigDecimal.ZERO) == 0 ? value : value.divide(divisor, 4, java.math.RoundingMode.HALF_UP);
    }

    private Map<String, Object> parseFormula(String formulaJson) {
        if (StringUtils.isBlank(formulaJson)) {
            return Map.of();
        }
        try {
            return JsonUtils.parseObject(formulaJson, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private BigDecimal dimension(PriceCalculationBo bo, String fieldName, String inputName) {
        Object value = "width".equals(fieldName) ? bo.getWidth() : bo.getHeight();
        if (value == null) {
            value = mapValue(bo.getInputValues(), fieldName);
        }
        if (value == null) {
            value = mapValue(bo.getInputValues(), inputName);
        }
        return defaultBigDecimal(value, BigDecimal.ZERO);
    }

    private BigDecimal defaultBigDecimal(Object value, BigDecimal defaultValue) {
        BigDecimal parsed = toBigDecimal(value);
        return parsed == null ? defaultValue : parsed;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
