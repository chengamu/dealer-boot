package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductShippingTemplateRuleBo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class ProductShippingTemplateRuleValidator {

    void validate(List<ProductShippingTemplateRuleBo> rules) {
        List<ProductShippingTemplateRuleBo> rows = rules == null ? List.of() : rules;
        Set<String> supportedCodes = Set.of(
            ProductShippingRuleSupport.CODE_MANUAL,
            ProductShippingRuleSupport.CODE_MOTORIZED
        );
        if (rows.stream().anyMatch(rule -> !supportedCodes.contains(rule.getFeeCode()))) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.scenarioInvalid");
        }
        for (String code : supportedCodes) {
            List<ProductShippingTemplateRuleBo> conditionRules = rows.stream()
                .filter(rule -> code.equals(rule.getFeeCode()))
                .toList();
            if (conditionRules.isEmpty()) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.ruleRequired");
            }
            validateRanges(conditionRules);
        }
        if (rows.stream().anyMatch(rule -> rule.getFeeAmount() == null
            || rule.getFeeAmount().compareTo(BigDecimal.ZERO) < 0)) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.feeAmountRequired");
        }
    }

    private void validateRanges(List<ProductShippingTemplateRuleBo> rules) {
        List<ProductShippingTemplateRuleBo> sorted = rules.stream()
            .sorted(Comparator.comparing(row -> row.getMinAreaSqft() == null ? BigDecimal.ZERO : row.getMinAreaSqft()))
            .toList();
        BigDecimal previousMax = null;
        for (int index = 0; index < sorted.size(); index++) {
            ProductShippingTemplateRuleBo rule = sorted.get(index);
            BigDecimal min = rule.getMinAreaSqft() == null ? BigDecimal.ZERO : rule.getMinAreaSqft();
            BigDecimal max = rule.getMaxAreaSqft();
            if (index == 0 && min.compareTo(BigDecimal.ZERO) != 0) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeCoverage");
            }
            if (max != null && max.compareTo(min) <= 0) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeInvalid");
            }
            if (index > 0) {
                if (previousMax == null || min.compareTo(previousMax) < 0) {
                    throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeOverlap");
                }
                if (min.compareTo(previousMax) > 0) {
                    throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeCoverage");
                }
            }
            previousMax = max;
        }
        if (previousMax != null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeCoverage");
        }
    }
}
