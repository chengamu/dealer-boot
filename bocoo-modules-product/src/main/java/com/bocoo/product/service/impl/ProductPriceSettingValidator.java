package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.vo.ProductPriceOptionCombinationVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceSettingValidator {

    private final ProductPriceSnapshotReader snapshotReader;

    public List<ProductPriceValidationIssueVo> validate(ProductFormulaVersion version,
                                                        List<ProductPriceFabric> fabrics,
                                                        List<ProductPriceFabricRule> fabricRules,
                                                        List<ProductPriceFeeRule> feeRules) {
        List<ProductPriceValidationIssueVo> issues = new ArrayList<>();
        if (version == null || !"EFFECTIVE".equals(version.getVersionStatus())) {
            issues.add(issue("ERROR", "FORMULA_VERSION", null, "product.priceSetting.formulaVersionRequired"));
        }
        List<Map<String, Object>> fabricMaterials = snapshotReader.fabricMaterials(version);
        validateFabrics(fabricMaterials, fabrics, fabricRules, issues);
        for (ProductPriceFeeRule rule : feeRules) {
            validateFeeRule(rule, issues);
        }
        validateShippingRules(feeRules, issues);
        return issues;
    }

    public Map<String, Integer> materialGroupCounts(ProductFormulaVersion version) {
        return snapshotReader.materialGroupCounts(version);
    }

    private void validateFabrics(List<Map<String, Object>> expectedMaterials, List<ProductPriceFabric> fabrics,
                                 List<ProductPriceFabricRule> rules, List<ProductPriceValidationIssueVo> issues) {
        if (!expectedMaterials.isEmpty() && fabrics.isEmpty()) {
            issues.add(issue("ERROR", "FABRIC", null, "product.priceSetting.fabricRuleRequired"));
        }
        Set<String> expectedCodes = expectedMaterials.stream()
            .map(material -> String.valueOf(material.get("materialCode")))
            .collect(Collectors.toSet());
        Set<String> actualCodes = fabrics.stream()
            .map(ProductPriceFabric::getMaterialCode)
            .collect(Collectors.toSet());
        for (String expectedCode : expectedCodes) {
            if (!actualCodes.contains(expectedCode)) {
                issues.add(issue("ERROR", "FABRIC_PRICE", expectedCode, "product.priceSetting.fabricPriceMissing"));
            }
        }
        Map<Long, List<ProductPriceFabricRule>> rulesByFabric = rules.stream()
            .collect(Collectors.groupingBy(ProductPriceFabricRule::getPriceFabricId));
        for (ProductPriceFabric fabric : fabrics) {
            validateFabric(fabric, expectedCodes, rulesByFabric.getOrDefault(fabric.getPriceFabricId(), List.of()), issues);
        }
    }

    private void validateFabric(ProductPriceFabric fabric, Set<String> expectedCodes, List<ProductPriceFabricRule> rules,
                                List<ProductPriceValidationIssueVo> issues) {
        if (StringUtils.isBlank(fabric.getMaterialCode())) {
            issues.add(issue("ERROR", "FABRIC_RULE", null, "product.priceSetting.materialRequired"));
        }
        if (!expectedCodes.contains(fabric.getMaterialCode())) {
            issues.add(issue("ERROR", "FABRIC_PRICE", fabric.getMaterialCode(), "product.priceSetting.fabricPriceNotInFormulaVersion"));
        }
        if (rules.stream().noneMatch(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag()))) {
            issues.add(issue("ERROR", "FABRIC_RULE", fabric.getMaterialCode(), "product.priceSetting.defaultFabricPriceRequired"));
        }
        for (ProductPriceFabricRule rule : rules) {
            validateFabricRule(fabric, rule, issues);
        }
    }

    private void validateFabricRule(ProductPriceFabric fabric, ProductPriceFabricRule rule, List<ProductPriceValidationIssueVo> issues) {
        if (ProductPriceExpressionValidator.usesUnitPrice(rule.getPriceFormula()) && isNotPositive(rule.getUnitPrice())) {
            issues.add(issue("ERROR", "FABRIC_RULE", fabric.getMaterialCode(), "product.priceSetting.fabricPriceRequired"));
        }
        if (!ProductPriceExpressionValidator.isPriceFormulaValid(rule.getPriceFormula())) {
            issues.add(issue("ERROR", "FABRIC_RULE", fabric.getMaterialCode(), "product.priceSetting.priceFormulaInvalid"));
        }
        if (!ProductPriceExpressionValidator.isConditionValid(rule.getConditionExpression())) {
            issues.add(issue("ERROR", "FABRIC_RULE", fabric.getMaterialCode(), "product.priceSetting.conditionInvalid"));
        }
    }

    private void validateFeeRule(ProductPriceFeeRule rule, List<ProductPriceValidationIssueVo> issues) {
        if (StringUtils.isBlank(rule.getFeeName())) {
            issues.add(issue("ERROR", "FEE_RULE", rule.getFeeCode(), "product.priceSetting.feeNameRequired"));
        }
        if (StringUtils.isBlank(rule.getFormulaText())) {
            issues.add(issue("ERROR", "FEE_RULE", rule.getFeeCode(), "product.priceSetting.shippingFormulaRequired"));
        } else if (!ProductPriceExpressionValidator.isShippingFormulaValid(rule.getFormulaText())) {
            issues.add(issue("ERROR", "FEE_RULE", rule.getFeeCode(), "product.priceSetting.priceFormulaInvalid"));
        }
        if (rule.getMinAreaSqft() != null && rule.getMaxAreaSqft() != null
            && rule.getMaxAreaSqft().compareTo(rule.getMinAreaSqft()) <= 0) {
            issues.add(issue("ERROR", "FEE_RULE", rule.getFeeCode(), "product.shippingTemplate.areaRangeInvalid"));
        }
    }

    public List<Map<String, Object>> fabricMaterials(ProductFormulaVersion version) {
        return snapshotReader.fabricMaterials(version);
    }

    public Map<String, Map<String, Object>> priceSnapshotByMaterialCode(ProductFormulaVersion version) {
        return snapshotReader.priceSnapshotByMaterialCode(version);
    }

    public List<ProductPriceOptionCombinationVo> optionCombinations(ProductFormulaVersion version) {
        return snapshotReader.optionCombinations(version);
    }

    private void validateShippingRules(List<ProductPriceFeeRule> feeRules, List<ProductPriceValidationIssueVo> issues) {
        Set<String> codes = feeRules.stream().map(ProductPriceFeeRule::getFeeCode).collect(Collectors.toSet());
        for (String code : List.of("MANUAL", "MOTORIZED")) {
            if (!codes.contains(code)) {
                issues.add(issue("ERROR", "SHIPPING", code, "product.priceSetting.shippingRuleRequired"));
            }
        }
    }

    private boolean isNotPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }

    private ProductPriceValidationIssueVo issue(String level, String sourceType, String sourceCode, String key) {
        return new ProductPriceValidationIssueVo(level, sourceType, sourceCode, key, key);
    }
}
