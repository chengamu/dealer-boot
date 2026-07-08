package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ProductPriceFabricRuleGuard {

    public void assertSavable(ProductPriceFabric fabric, List<ProductPriceFabricRuleBo> rules) {
        if (fabric == null) {
            throw ServiceException.ofMessageKey("product.priceSetting.fabricPriceNotInFormulaVersion");
        }
        Set<String> seenKeys = new HashSet<>();
        boolean hasDefault = false;
        for (ProductPriceFabricRuleBo rule : rules == null ? List.<ProductPriceFabricRuleBo>of() : rules) {
            String key = ruleKey(rule);
            if (!seenKeys.add(key)) {
                throw ServiceException.ofMessageKey("product.priceSetting.duplicateFabricCondition");
            }
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
                hasDefault = true;
            }
            if (isNotPositive(rule.getUnitPrice())) {
                throw ServiceException.ofMessageKey("product.priceSetting.fabricPriceRequired");
            }
            if (!ProductPriceExpressionValidator.isPriceFormulaValid(rule.getPriceFormula())) {
                throw ServiceException.ofMessageKey("product.priceSetting.priceFormulaInvalid");
            }
            if (!ProductPriceExpressionValidator.isConditionValid(rule.getConditionExpression())) {
                throw ServiceException.ofMessageKey("product.priceSetting.conditionInvalid");
            }
        }
        if (!hasDefault) {
            throw ServiceException.ofMessageKey("product.priceSetting.defaultFabricPriceRequired");
        }
    }

    private String ruleKey(ProductPriceFabricRuleBo rule) {
        if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
            return "DEFAULT";
        }
        return StringUtils.blankToDefault(rule.getConditionKey(), rule.getConditionExpression());
    }

    private boolean isNotPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }
}
