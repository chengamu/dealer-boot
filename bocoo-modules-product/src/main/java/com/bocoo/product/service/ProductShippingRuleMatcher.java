package com.bocoo.product.service;

import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public final class ProductShippingRuleMatcher {

    private ProductShippingRuleMatcher() {
    }

    public static ProductShippingTemplateRuleVo match(List<ProductShippingTemplateRuleVo> rules,
                                                       String feeCode, BigDecimal area) {
        if (rules == null || feeCode == null || area == null) {
            return null;
        }
        return rules.stream()
            .filter(row -> feeCode.equalsIgnoreCase(row.getFeeCode()))
            .filter(row -> row.getMinAreaSqft() == null || area.compareTo(row.getMinAreaSqft()) >= 0)
            .filter(row -> row.getMaxAreaSqft() == null || area.compareTo(row.getMaxAreaSqft()) < 0)
            .min(Comparator.comparing(row -> row.getSortOrder() == null ? Integer.MAX_VALUE : row.getSortOrder()))
            .orElse(null);
    }
}
