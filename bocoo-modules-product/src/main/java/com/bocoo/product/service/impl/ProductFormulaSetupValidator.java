package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import com.bocoo.product.service.ProductFormulaVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaSetupValidator extends ProductServiceSupport {

    private static final String VISIBILITY_CONDITIONAL = "CONDITIONAL";

    private final ProductFormulaUsageRuleService usageRuleService;
    private final ProductFormulaVariableService variableService;

    String validationMessageKey(ProductFormulaSetupContext context) {
        String materialMessageKey = materialValidationMessageKey(context);
        return materialMessageKey == null ? optionValidationMessageKey(context) : materialMessageKey;
    }

    String materialValidationMessageKey(ProductFormulaSetupContext context) {
        Map<String, List<ProductFormulaUsageRule>> usageRulesByMaterial = context.usageRules().stream()
            .filter(rule -> STATUS_ENABLED.equals(rule.getStatus()))
            .collect(Collectors.groupingBy(ProductFormulaUsageRule::getMaterialCode));
        if (context.materials().isEmpty()) {
            return "product.formula.notConfigured";
        }
        for (ProductFormulaMaterial material : context.materials()) {
            if (material.getMaterialId() == null && StringUtils.isBlank(material.getMaterialCode())) {
                return "product.formula.materialRequired";
            }
            if (StringUtils.isBlank(material.getUnitCode())) {
                return "product.formula.materialUnitRequired";
            }
            if (usageRulesByMaterial.containsKey(material.getMaterialCode())) {
                continue;
            }
            if (StringUtils.isBlank(material.getUsageMode())) {
                return "product.formula.materialUsageModeRequired";
            }
            if ("FORMULA".equals(material.getUsageMode()) && StringUtils.isBlank(material.getUsageFormula())) {
                return "product.formula.materialUsageRuleRequired";
            }
            if ("FIXED".equals(material.getUsageMode()) && material.getFixedUsageQty() == null) {
                return "product.formula.materialUsageRuleRequired";
            }
        }
        String usageMessageKey = usageRuleService.validationMessageKey(context.materials(), context.options(), context.values(), context.usageRules());
        if (usageMessageKey != null) {
            return usageMessageKey;
        }
        return variableService.validationMessageKey(context.variables(), context.variableRules(), context.usageRules());
    }

    String optionValidationMessageKey(ProductFormulaSetupContext context) {
        if (context.materials().isEmpty()) {
            return "product.formula.notConfigured";
        }
        if (context.options().isEmpty()) {
            return "product.formula.optionRequired";
        }
        Map<String, ProductFormulaOption> optionMap = context.options().stream()
            .filter(option -> StringUtils.isNotBlank(option.getOptionCode()))
            .collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity(), (left, right) -> left));
        Set<String> optionCodes = optionMap.keySet();
        Map<String, Long> optionValueCounts = context.values().stream()
            .filter(value -> StringUtils.isNotBlank(value.getOptionCode()))
            .collect(Collectors.groupingBy(ProductFormulaOptionValue::getOptionCode, Collectors.counting()));
        Set<String> valueKeys = context.values().stream()
            .map(value -> key(value.getOptionCode(), value.getValueCode()))
            .collect(Collectors.toSet());
        for (ProductFormulaOption option : context.options()) {
            if (StringUtils.isBlank(option.getOptionCode()) || StringUtils.isBlank(option.getOptionNameCn())) {
                return "product.formula.optionRequired";
            }
            if (optionValueCounts.getOrDefault(option.getOptionCode(), 0L) == 0L) {
                return "product.formula.optionValueRequired";
            }
            String visibilityMessageKey = validateOptionVisibility(option, optionMap, valueKeys);
            if (visibilityMessageKey != null) {
                return visibilityMessageKey;
            }
        }
        Set<String> materialCodes = context.materials().stream()
            .map(ProductFormulaMaterial::getMaterialCode)
            .collect(Collectors.toSet());
        Set<String> optionMaterialKeys = new HashSet<>();
        for (ProductFormulaOptionMaterial optionMaterial : context.optionMaterials()) {
            if (!optionCodes.contains(optionMaterial.getOptionCode())
                || !valueKeys.contains(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode()))) {
                return "product.formula.optionMaterialValueInvalid";
            }
            if (!materialCodes.contains(optionMaterial.getMaterialCode())) {
                return "product.formula.optionMaterialNotInPool";
            }
            if (!optionMaterialKeys.add(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode(), optionMaterial.getMaterialCode()))) {
                return "product.formula.optionMaterialDuplicate";
            }
        }
        for (ProductFormulaRestriction restriction : context.restrictions()) {
            String restrictionMessageKey = validateRestriction(restriction, optionCodes, valueKeys);
            if (restrictionMessageKey != null) {
                return restrictionMessageKey;
            }
            if (StringUtils.isNotBlank(restriction.getTargetOptionCode()) && !optionCodes.contains(restriction.getTargetOptionCode())) {
                return "product.formula.restrictionTargetInvalid";
            }
        }
        return null;
    }

    String validateRestriction(ProductFormulaRestriction restriction, Set<String> optionCodes, Set<String> valueKeys) {
        if (StringUtils.isBlank(restriction.getTargetOptionCode()) || !optionCodes.contains(restriction.getTargetOptionCode())) {
            return "product.formula.restrictionTargetInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getTargetValueCode())
            && !valueKeys.contains(key(restriction.getTargetOptionCode(), restriction.getTargetValueCode()))) {
            return "product.formula.restrictionTargetInvalid";
        }
        if (StringUtils.isBlank(restriction.getConditionType()) || StringUtils.isBlank(restriction.getConditionOperator())
            || StringUtils.isBlank(restriction.getActionType())) {
            return "product.formula.restrictionConditionInvalid";
        }
        if ("OPTION_VALUE".equals(restriction.getConditionType())) {
            if (StringUtils.isBlank(restriction.getConditionOptionCode()) || !optionCodes.contains(restriction.getConditionOptionCode())) {
                return "product.formula.restrictionConditionInvalid";
            }
            if (StringUtils.isBlank(restriction.getConditionValueCode())
                || !valueKeys.contains(key(restriction.getConditionOptionCode(), restriction.getConditionValueCode()))) {
                return "product.formula.restrictionConditionInvalid";
            }
            return null;
        }
        if (("WIDTH".equals(restriction.getConditionType()) || "HEIGHT".equals(restriction.getConditionType())
            || "WEIGHT".equals(restriction.getConditionType())) && restriction.getConditionValueNumber() == null) {
            return "product.formula.restrictionConditionInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getConditionOptionCode()) && !optionCodes.contains(restriction.getConditionOptionCode())) {
            return "product.formula.restrictionConditionInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getConditionValueCode())
            && (StringUtils.isBlank(restriction.getConditionOptionCode())
            || !valueKeys.contains(key(restriction.getConditionOptionCode(), restriction.getConditionValueCode())))) {
            return "product.formula.restrictionConditionInvalid";
        }
        return null;
    }

    private String validateOptionVisibility(ProductFormulaOption option, Map<String, ProductFormulaOption> optionMap, Set<String> valueKeys) {
        if (!VISIBILITY_CONDITIONAL.equals(option.getVisibilityMode())) {
            return null;
        }
        if (StringUtils.isBlank(option.getVisibleConditionOptionCode())) {
            return "product.formula.optionVisibilityConditionRequired";
        }
        if (option.getVisibleConditionOptionCode().equals(option.getOptionCode())) {
            return "product.formula.optionVisibilitySelfDenied";
        }
        if (!optionMap.containsKey(option.getVisibleConditionOptionCode())) {
            return "product.formula.optionVisibilityConditionInvalid";
        }
        if (StringUtils.isBlank(option.getVisibleConditionValueCode())) {
            return "product.formula.optionVisibilityValueRequired";
        }
        if (!valueKeys.contains(key(option.getVisibleConditionOptionCode(), option.getVisibleConditionValueCode()))) {
            return "product.formula.optionVisibilityValueInvalid";
        }
        return null;
    }

    private String key(String... parts) {
        return String.join("|", parts);
    }
}
