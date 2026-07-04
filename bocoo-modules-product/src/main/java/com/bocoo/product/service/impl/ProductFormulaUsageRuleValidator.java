package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaUsageRuleValidator {

    private static final String CONDITION_DEFAULT = "DEFAULT";
    private static final String CONDITION_OPTION_VALUE = "OPTION_VALUE";
    private static final String CONDITION_EXPRESSION = "EXPRESSION";
    private static final String STATUS_ENABLED = ProductServiceSupport.STATUS_ENABLED;
    private static final String USAGE_FIXED = "FIXED";
    private static final String USAGE_FORMULA = "FORMULA";
    private final ProductFormulaExpressionReferenceValidator referenceValidator;

    public String validationMessageKey(List<ProductFormulaMaterial> materials, List<ProductFormulaOption> options,
                                       List<ProductFormulaOptionValue> values, List<ProductFormulaOptionMaterial> optionMaterials,
                                       List<ProductFormulaUsageRule> usageRules) {
        Map<String, ProductFormulaMaterial> materialByCode = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, Function.identity(), (left, right) -> left));
        Set<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode).collect(Collectors.toSet());
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        Map<String, List<ProductFormulaUsageRule>> rulesByMaterial = usageRules.stream()
            .filter(rule -> STATUS_ENABLED.equals(rule.getStatus()))
            .collect(Collectors.groupingBy(ProductFormulaUsageRule::getMaterialCode));
        for (ProductFormulaUsageRule rule : usageRules) {
            String messageKey = validateRuleReference(rule, materialByCode, optionCodes, valueKeys, options, values, optionMaterials, materials);
            if (messageKey != null) {
                return messageKey;
            }
            messageKey = validateRuleContent(rule);
            if (messageKey != null) {
                return messageKey;
            }
        }
        for (Map.Entry<String, List<ProductFormulaUsageRule>> entry : rulesByMaterial.entrySet()) {
            Set<String> modes = entry.getValue().stream().map(ProductFormulaUsageRule::getUsageMode).collect(Collectors.toSet());
            if (modes.size() > 1) {
                return "product.formula.usageModeMixedInvalid";
            }
            if (modes.contains(USAGE_FIXED)) {
                if (entry.getValue().size() > 1) {
                    return "product.formula.usageRuleDuplicate";
                }
                continue;
            }
            if (modes.contains(USAGE_FORMULA)) {
                long defaultCount = entry.getValue().stream().filter(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag())).count();
                if (defaultCount == 0) {
                    return "product.formula.usageDefaultRuleRequired";
                }
                if (defaultCount > 1) {
                    return "product.formula.usageRuleDuplicate";
                }
            }
        }
        return null;
    }

    public String validateRuleContent(ProductFormulaUsageRule rule) {
        if (StringUtils.isBlank(rule.getUsageMode())) {
            return "product.formula.materialUsageModeRequired";
        }
        if (!USAGE_FIXED.equals(rule.getUsageMode()) && !USAGE_FORMULA.equals(rule.getUsageMode())) {
            return "product.formula.materialUsageModeRequired";
        }
        if (USAGE_FIXED.equals(rule.getUsageMode()) && (rule.getFixedUsageQty() == null || rule.getFixedUsageQty().compareTo(BigDecimal.ZERO) < 0)) {
            return "product.formula.materialUsageRuleRequired";
        }
        if (USAGE_FORMULA.equals(rule.getUsageMode())) {
            if (!hasAnyFormula(rule)) {
                return "product.formula.materialUsageRuleRequired";
            }
            if (!isFormulaValidIfPresent(rule.getLengthFormula())
                || !isFormulaValidIfPresent(rule.getWidthFormula())
                || !isFormulaValidIfPresent(rule.getHeightFormula())
                || !isFormulaValidIfPresent(rule.getWeightFormula())
                || !isFormulaValidIfPresent(rule.getUsageFormula())) {
                return "product.formula.usageFormulaInvalid";
            }
        }
        return null;
    }

    private String validateRuleReference(ProductFormulaUsageRule rule, Map<String, ProductFormulaMaterial> materialByCode,
                                         Set<String> optionCodes, Set<String> valueKeys, List<ProductFormulaOption> options,
                                         List<ProductFormulaOptionValue> values, List<ProductFormulaOptionMaterial> optionMaterials,
                                         List<ProductFormulaMaterial> materials) {
        if (!materialByCode.containsKey(rule.getMaterialCode())) {
            return "product.formula.usageMaterialInvalid";
        }
        if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || CONDITION_DEFAULT.equals(rule.getConditionType())) {
            return null;
        }
        if (!CONDITION_OPTION_VALUE.equals(rule.getConditionType())) {
            if (!CONDITION_EXPRESSION.equals(rule.getConditionType())
                || !ProductFormulaExpressionValidator.isConditionValid(rule.getConditionExpression())) {
                return "product.formula.usageConditionInvalid";
            }
            String messageKey = referenceValidator.validationMessageKey(rule.getConditionExpression(), options, values, optionMaterials, materials);
            return messageKey == null ? null : "product.formula.usageConditionInvalid";
        }
        if (!optionCodes.contains(rule.getConditionOptionCode())
            || !valueKeys.contains(key(rule.getConditionOptionCode(), rule.getConditionValueCode()))) {
            return "product.formula.usageConditionInvalid";
        }
        return null;
    }

    private boolean hasAnyFormula(ProductFormulaUsageRule rule) {
        return StringUtils.isNotBlank(rule.getLengthFormula())
            || StringUtils.isNotBlank(rule.getWidthFormula())
            || StringUtils.isNotBlank(rule.getHeightFormula())
            || StringUtils.isNotBlank(rule.getWeightFormula())
            || StringUtils.isNotBlank(rule.getUsageFormula());
    }

    private boolean isFormulaValidIfPresent(String formula) {
        return StringUtils.isBlank(formula) || ProductFormulaExpressionValidator.isFormulaValid(formula);
    }

    private String key(String... parts) {
        return java.util.Arrays.stream(parts)
            .map(part -> part == null ? "" : part)
            .collect(Collectors.joining("|"));
    }
}
