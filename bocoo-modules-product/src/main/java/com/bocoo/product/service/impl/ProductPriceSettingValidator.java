package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceSettingValidator {
    private final ProductPriceSnapshotReader snapshotReader;
    private final ProductPriceConditionSnapshotFactory conditionSnapshotFactory;

    public List<ProductPriceValidationIssueVo> validate(ProductFormulaVersion version,
                                                        List<ProductPriceMaterial> materials,
                                                        List<ProductPriceMaterialRule> rules) {
        List<ProductPriceValidationIssueVo> issues = new ArrayList<>();
        if (version == null || !"EFFECTIVE".equals(version.getVersionStatus())) {
            issues.add(issue("ERROR", "FORMULA_VERSION", null, "product.priceSetting.formulaVersionRequired"));
        }
        List<Map<String, Object>> expected = snapshotReader.priceMaterials(version);
        if (!expected.isEmpty() && materials.isEmpty()) {
            issues.add(issue("ERROR", "MATERIAL_PRICE", null, "product.priceSetting.materialRuleRequired"));
        }
        Set<String> expectedKeys = expected.stream().map(this::materialKey).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> actualKeys = materials.stream().map(this::materialKey).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        expectedKeys.stream().filter(key -> !actualKeys.contains(key)).forEach(key ->
            issues.add(issue("ERROR", "MATERIAL_PRICE", key, "product.priceSetting.materialPriceMissing")));
        Map<Long, List<ProductPriceMaterialRule>> rulesByMaterial = rules.stream()
            .collect(Collectors.groupingBy(ProductPriceMaterialRule::getPriceMaterialId));
        for (ProductPriceMaterial material : materials) {
            validateMaterial(version, material, expectedKeys,
                rulesByMaterial.getOrDefault(material.getPriceMaterialId(), List.of()), issues);
        }
        return issues;
    }

    private void validateMaterial(ProductFormulaVersion version, ProductPriceMaterial material, Set<String> expectedKeys,
                                  List<ProductPriceMaterialRule> rules, List<ProductPriceValidationIssueVo> issues) {
        if (StringUtils.isBlank(material.getMaterialCode())) {
            issues.add(issue("ERROR", "MATERIAL_RULE", null, "product.priceSetting.materialRequired"));
        }
        if (!expectedKeys.contains(materialKey(material))) {
            issues.add(issue("ERROR", "MATERIAL_PRICE", material.getMaterialCode(), "product.priceSetting.materialPriceNotInFormulaVersion"));
        }
        long defaultCount = rules.stream().filter(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag())).count();
        if (defaultCount != 1) {
            issues.add(issue("ERROR", "MATERIAL_RULE", material.getMaterialCode(), "product.priceSetting.defaultMaterialPriceRequired"));
        }
        Set<String> keys = new HashSet<>();
        for (ProductPriceMaterialRule rule : rules) {
            if (!keys.add(StringUtils.blankToDefault(rule.getConditionKey(), rule.getConditionExpression()))) {
                issues.add(issue("ERROR", "MATERIAL_RULE", material.getMaterialCode(), "product.priceSetting.duplicateMaterialCondition"));
            }
            validateRule(version, material, rule, issues);
        }
    }

    private void validateRule(ProductFormulaVersion version, ProductPriceMaterial material, ProductPriceMaterialRule rule,
                              List<ProductPriceValidationIssueVo> issues) {
        if (ProductPriceExpressionValidator.usesUnitPrice(rule.getPriceFormula()) && isNotPositive(rule.getUnitPrice())) {
            issues.add(issue("ERROR", "MATERIAL_RULE", material.getMaterialCode(), "product.priceSetting.materialPriceRequired"));
        }
        if (!ProductPriceExpressionValidator.isPriceFormulaValid(rule.getPriceFormula())) {
            issues.add(issue("ERROR", "MATERIAL_RULE", material.getMaterialCode(), "product.priceSetting.priceFormulaInvalid"));
        }
        if (!validConditionReference(version, rule)) {
            issues.add(issue("ERROR", "MATERIAL_RULE", material.getMaterialCode(), "product.priceSetting.conditionInvalid"));
        }
    }

    private boolean validConditionReference(ProductFormulaVersion version, ProductPriceMaterialRule rule) {
        if (!ProductPriceExpressionValidator.isConditionValid(rule.getConditionExpression())
            || StringUtils.isBlank(rule.getConditionJson())) {
            return false;
        }
        try {
            conditionSnapshotFactory.snapshot(version, rule.getConditionExpression(), rule.getConditionText());
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private String materialKey(Map<String, Object> source) {
        Object materialId = source.get("materialId");
        return materialId == null ? codeKey(source.get("materialCode")) : "ID:" + materialId;
    }

    private String materialKey(ProductPriceMaterial material) {
        return material.getMaterialId() == null ? codeKey(material.getMaterialCode()) : "ID:" + material.getMaterialId();
    }

    private String codeKey(Object code) {
        return code == null || StringUtils.isBlank(String.valueOf(code)) ? null : "CODE:" + code;
    }

    private boolean isNotPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }

    private ProductPriceValidationIssueVo issue(String level, String sourceType, String sourceCode, String key) {
        return new ProductPriceValidationIssueVo(level, sourceType, sourceCode, key, key);
    }
}
