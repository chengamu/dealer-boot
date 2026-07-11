package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductPriceMaterialRuleBo;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ProductPriceMaterialRuleGuard {

    public void assertSavable(ProductPriceMaterial material, List<ProductPriceMaterialRuleBo> rules) {
        if (material == null) {
            throw ServiceException.ofMessageKey("product.priceSetting.materialPriceNotInFormulaVersion");
        }
        Set<String> seenKeys = new HashSet<>();
        boolean hasDefault = false;
        for (ProductPriceMaterialRuleBo rule : rules == null ? List.<ProductPriceMaterialRuleBo>of() : rules) {
            String key = ruleKey(rule);
            if (!seenKeys.add(key)) {
                throw ServiceException.ofMessageKey("product.priceSetting.duplicateMaterialCondition");
            }
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
                hasDefault = true;
            }
            if (ProductPriceExpressionValidator.usesUnitPrice(rule.getPriceFormula()) && isNotPositive(rule.getUnitPrice())) {
                throw ServiceException.ofMessageKey("product.priceSetting.materialPriceRequired");
            }
            if (!ProductPriceExpressionValidator.isPriceFormulaValid(rule.getPriceFormula())) {
                throw ServiceException.ofMessageKey("product.priceSetting.priceFormulaInvalid");
            }
            if (!ProductPriceExpressionValidator.isConditionValid(rule.getConditionExpression())) {
                throw ServiceException.ofMessageKey("product.priceSetting.conditionInvalid");
            }
        }
        if (!hasDefault) {
            throw ServiceException.ofMessageKey("product.priceSetting.defaultMaterialPriceRequired");
        }
    }

    private String ruleKey(ProductPriceMaterialRuleBo rule) {
        if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
            return "DEFAULT";
        }
        return StringUtils.blankToDefault(rule.getConditionKey(), rule.getConditionExpression());
    }

    private boolean isNotPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }
}
