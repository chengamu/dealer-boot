package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class CustomerQuoteOptionLabelResolver {

    String selectedValue(Map<String, String> selected, ProductFormulaOptionVo option) {
        String value = selected.get(option.getOptionCode());
        return StringUtils.isNotBlank(value) ? value : selected.get(option.getOptionRefKey());
    }

    String displayCn(ProductFormulaOptionVo option, ProductFormulaOptionValueVo value, String selectedValue) {
        String optionName = first(option.getOptionNameCn(), option.getOptionNameEn(), option.getOptionCode());
        String valueName = value == null ? selectedValue
            : first(value.getValueNameCn(), value.getValueNameEn(), value.getValueCode());
        return optionName + "：" + valueName;
    }

    String displayEn(ProductFormulaOptionVo option, ProductFormulaOptionValueVo value) {
        if (value == null || StringUtils.isBlank(option.getOptionNameEn())
            || StringUtils.isBlank(value.getValueNameEn())) {
            return null;
        }
        return option.getOptionNameEn() + ": " + value.getValueNameEn();
    }

    private String first(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }
}
