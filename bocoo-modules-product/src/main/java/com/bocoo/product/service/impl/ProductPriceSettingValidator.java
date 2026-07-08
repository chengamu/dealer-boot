package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
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
                                                        List<ProductPriceFabricRule> fabricRules,
                                                        List<ProductPriceFeeRule> feeRules) {
        List<ProductPriceValidationIssueVo> issues = new ArrayList<>();
        if (version == null || !"EFFECTIVE".equals(version.getVersionStatus())) {
            issues.add(issue("ERROR", "FORMULA_VERSION", null, "product.priceSetting.formulaVersionRequired"));
        }
        List<Map<String, Object>> fabricMaterials = snapshotReader.fabricMaterials(version);
        if (!fabricMaterials.isEmpty() && fabricRules.isEmpty()) {
            issues.add(issue("ERROR", "FABRIC_RULE", null, "product.priceSetting.fabricRuleRequired"));
        }
        validateFabricMatrix(version, fabricRules, issues);
        for (ProductPriceFeeRule rule : feeRules) {
            validateFeeRule(rule, issues);
        }
        validateShippingRules(feeRules, issues);
        return issues;
    }

    public Map<String, Integer> materialGroupCounts(ProductFormulaVersion version) {
        return snapshotReader.materialGroupCounts(version);
    }

    private void validateFabricMatrix(ProductFormulaVersion version, List<ProductPriceFabricRule> rules,
                                      List<ProductPriceValidationIssueVo> issues) {
        Set<String> expectedKeys = snapshotReader.fabricMaterials(version).stream()
            .map(material -> String.valueOf(material.get("materialCode")))
            .flatMap(materialCode -> snapshotReader.optionCombinations(version).stream()
                .map(combo -> fabricKey(materialCode, combo.getOptionCombinationKey())))
            .collect(Collectors.toSet());
        Set<String> actualKeys = rules.stream()
            .map(rule -> fabricKey(rule.getMaterialCode(), rule.getOptionCombinationKey()))
            .collect(Collectors.toSet());
        for (String expectedKey : expectedKeys) {
            if (!actualKeys.contains(expectedKey)) {
                issues.add(issue("ERROR", "FABRIC_PRICE", expectedKey, "product.priceSetting.fabricPriceMissing"));
            }
        }
        for (ProductPriceFabricRule rule : rules) {
            validateFabricRule(rule, expectedKeys, issues);
        }
    }

    private void validateFabricRule(ProductPriceFabricRule rule, Set<String> expectedKeys, List<ProductPriceValidationIssueVo> issues) {
        if (StringUtils.isBlank(rule.getMaterialCode())) {
            issues.add(issue("ERROR", "FABRIC_RULE", null, "product.priceSetting.materialRequired"));
        }
        String rowKey = fabricKey(rule.getMaterialCode(), rule.getOptionCombinationKey());
        if (!expectedKeys.contains(rowKey)) {
            issues.add(issue("ERROR", "FABRIC_PRICE", rowKey, "product.priceSetting.fabricPriceNotInFormulaVersion"));
        }
        if (isNotPositive(rule.getBasePrice())) {
            issues.add(issue("ERROR", "FABRIC_RULE", rule.getMaterialCode(), "product.priceSetting.fabricPriceRequired"));
        }
        if (StringUtils.isBlank(rule.getAreaFormula())) {
            issues.add(issue("ERROR", "FABRIC_RULE", rule.getMaterialCode(), "product.priceSetting.areaFormulaRequired"));
        }
    }

    private void validateFeeRule(ProductPriceFeeRule rule, List<ProductPriceValidationIssueVo> issues) {
        if (StringUtils.isBlank(rule.getFeeName())) {
            issues.add(issue("ERROR", "FEE_RULE", rule.getFeeCode(), "product.priceSetting.feeNameRequired"));
        }
        if (StringUtils.isBlank(rule.getFormulaText())) {
            issues.add(issue("ERROR", "FEE_RULE", rule.getFeeCode(), "product.priceSetting.shippingFormulaRequired"));
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

    private String fabricKey(String materialCode, String optionCombinationKey) {
        return StringUtils.blankToDefault(materialCode, "") + "||"
            + StringUtils.blankToDefault(optionCombinationKey, "DEFAULT");
    }

    private boolean isNotPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }

    private ProductPriceValidationIssueVo issue(String level, String sourceType, String sourceCode, String key) {
        return new ProductPriceValidationIssueVo(level, sourceType, sourceCode, key, key);
    }
}
