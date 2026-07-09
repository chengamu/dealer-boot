package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ProductFormulaOptionRefResolver {
    private final Map<String, ProductFormulaOption> optionByRef;
    private final Map<String, ProductFormulaOption> optionByCode;
    private final Map<String, ProductFormulaOptionValue> valueByRef;
    private final Map<String, ProductFormulaOptionValue> valueByCode;

    ProductFormulaOptionRefResolver(List<ProductFormulaOption> options, List<ProductFormulaOptionValue> values) {
        optionByRef = options.stream()
            .filter(row -> StringUtils.isNotBlank(row.getOptionRefKey()))
            .collect(Collectors.toMap(ProductFormulaOption::getOptionRefKey, Function.identity(), (left, right) -> left));
        optionByCode = options.stream()
            .filter(row -> StringUtils.isNotBlank(row.getOptionCode()))
            .collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity(), (left, right) -> left));
        valueByRef = values.stream()
            .filter(row -> StringUtils.isNotBlank(row.getValueRefKey()))
            .collect(Collectors.toMap(ProductFormulaOptionValue::getValueRefKey, Function.identity(), (left, right) -> left));
        valueByCode = values.stream()
            .filter(row -> StringUtils.isNotBlank(row.getOptionCode()) && StringUtils.isNotBlank(row.getValueCode()))
            .collect(Collectors.toMap(row -> key(row.getOptionCode(), row.getValueCode()), Function.identity(), (left, right) -> left));
    }

    ProductFormulaOption option(String optionRefKey, String optionCode) {
        ProductFormulaOption option = StringUtils.isBlank(optionRefKey) ? null : optionByRef.get(optionRefKey.trim());
        if (option == null && StringUtils.isNotBlank(optionCode)) {
            option = optionByCode.get(optionCode.trim().toUpperCase(java.util.Locale.ROOT));
        }
        return option;
    }

    ProductFormulaOptionValue value(ProductFormulaOption option, String valueRefKey, String valueCode) {
        ProductFormulaOptionValue value = StringUtils.isBlank(valueRefKey) ? null : valueByRef.get(valueRefKey.trim());
        if (value != null && option != null && !option.getOptionRefKey().equals(value.getOptionRefKey())) {
            value = null;
        }
        if (value == null && option != null && StringUtils.isNotBlank(valueCode)) {
            value = valueByCode.get(key(option.getOptionCode(), valueCode.trim().toUpperCase(java.util.Locale.ROOT)));
        }
        return value;
    }

    private static String key(String optionCode, String valueCode) {
        return optionCode + "|" + valueCode;
    }
}
