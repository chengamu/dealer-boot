package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

final class ProductFormulaConditionJsonFactory {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private ProductFormulaConditionJsonFactory() {
    }

    static String defaultCondition() {
        return json(Map.of("type", "DEFAULT"));
    }

    static String expression(String expression, String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "EXPRESSION");
        body.put("expression", StringUtils.blankToDefault(expression, ""));
        body.put("text", StringUtils.blankToDefault(text, expression));
        return json(body);
    }

    static String optionValue(ProductFormulaOption option, ProductFormulaOptionValue value) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "OPTION_VALUE");
        body.put("operator", "EQ");
        body.put("optionRefKey", option.getOptionRefKey());
        body.put("optionCodeSnapshot", option.getOptionCode());
        body.put("optionNameSnapshot", option.getOptionNameCn());
        body.put("valueRefKey", value.getValueRefKey());
        body.put("valueCodeSnapshot", value.getValueCode());
        body.put("valueNameSnapshot", value.getValueNameCn());
        return json(body);
    }

    static String measure(String field, String operator, BigDecimal value) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "MEASURE");
        body.put("field", StringUtils.blankToDefault(field, ""));
        body.put("operator", StringUtils.blankToDefault(operator, ""));
        body.put("value", value);
        return json(body);
    }

    private static String json(Map<String, ?> body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build formula condition json", e);
        }
    }
}
