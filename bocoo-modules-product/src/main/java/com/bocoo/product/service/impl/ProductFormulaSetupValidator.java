package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
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
    private static final String SOURCE_MATERIAL_POOL = "MATERIAL_POOL";

    private final ProductFormulaUsageRuleService usageRuleService;
    private final ProductFormulaVariableService variableService;
    private final ProductFormulaExpressionReferenceValidator referenceValidator;

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
        String usageMessageKey = usageRuleService.validationMessageKey(context.materials(), context.options(), context.values(),
            context.optionMaterials(), context.usageRules());
        if (usageMessageKey != null) {
            return usageMessageKey;
        }
        String variableConditionMessageKey = validateVariableRuleReferences(context);
        if (variableConditionMessageKey != null) {
            return variableConditionMessageKey;
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
        Set<String> optionCodes = optionTokens(context.options());
        Map<String, Long> optionValueCounts = context.values().stream()
            .filter(value -> StringUtils.isNotBlank(value.getOptionCode()))
            .collect(Collectors.groupingBy(ProductFormulaOptionValue::getOptionCode, Collectors.counting()));
        Set<String> valueKeys = valueKeys(context.values());
        for (ProductFormulaOption option : context.options()) {
            if (StringUtils.isBlank(option.getOptionCode()) || StringUtils.isBlank(option.getOptionNameCn())) {
                return "product.formula.optionRequired";
            }
            if (optionValueCounts.getOrDefault(option.getOptionCode(), 0L) == 0L) {
                return "product.formula.optionValueRequired";
            }
            String visibilityMessageKey = validateOptionVisibility(option, optionMap, optionCodes, valueKeys);
            if (visibilityMessageKey != null) {
                return visibilityMessageKey;
            }
        }
        Set<String> materialCodes = context.materials().stream()
            .map(ProductFormulaMaterial::getMaterialCode)
            .collect(Collectors.toSet());
        Set<String> optionMaterialKeys = new HashSet<>();
        Set<String> valuesWithMaterials = new HashSet<>();
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
            valuesWithMaterials.add(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode()));
        }
        Set<String> parentValueKeys = context.options().stream()
            .filter(option -> VISIBILITY_CONDITIONAL.equals(option.getVisibilityMode()))
            .filter(option -> StringUtils.isNotBlank(option.getVisibleConditionOptionCode())
                && StringUtils.isNotBlank(option.getVisibleConditionValueCode()))
            .map(option -> key(option.getVisibleConditionOptionCode(), option.getVisibleConditionValueCode()))
            .collect(Collectors.toSet());
        for (ProductFormulaOptionValue value : context.values()) {
            String valueKey = key(value.getOptionCode(), value.getValueCode());
            ProductFormulaOption ownerOption = optionMap.get(value.getOptionCode());
            if (requiresLinkedMaterial(ownerOption) && !parentValueKeys.contains(valueKey) && !valuesWithMaterials.contains(valueKey)) {
                return "product.formula.optionValueMaterialRequired";
            }
        }
        for (ProductFormulaRestriction restriction : context.restrictions()) {
            String restrictionMessageKey = validateRestriction(restriction, optionCodes, valueKeys, context);
            if (restrictionMessageKey != null) {
                return restrictionMessageKey;
            }
            if (StringUtils.isNotBlank(restriction.getTargetOptionCode()) && !optionCodes.contains(restriction.getTargetOptionCode())) {
                return "product.formula.restrictionTargetInvalid";
            }
        }
        return null;
    }

    String optionReferenceValidationMessageKey(ProductFormulaSetupContext context) {
        Set<String> optionCodes = optionTokens(context.options());
        Set<String> valueKeys = valueKeys(context.values());
        for (ProductFormulaRestriction restriction : context.restrictions()) {
            String restrictionMessageKey = validateRestriction(restriction, optionCodes, valueKeys, context);
            if (restrictionMessageKey != null) {
                return restrictionMessageKey;
            }
        }
        String usageMessageKey = validateUsageRuleOptionReferences(context, optionCodes, valueKeys);
        return usageMessageKey == null ? validateVariableRuleReferences(context) : usageMessageKey;
    }

    String validateRestriction(ProductFormulaRestriction restriction, Set<String> optionCodes, Set<String> valueKeys) {
        return validateRestriction(restriction, optionCodes, valueKeys, null);
    }

    String validateRestriction(ProductFormulaRestriction restriction, Set<String> optionCodes, Set<String> valueKeys,
                               ProductFormulaSetupContext context) {
        if (StringUtils.isNotBlank(restriction.getTargetOptionCode()) && !optionCodes.contains(restriction.getTargetOptionCode())) {
            return "product.formula.restrictionTargetInvalid";
        }
        if (StringUtils.isNotBlank(restriction.getTargetValueCode()) && (StringUtils.isBlank(restriction.getTargetOptionCode())
            || !valueKeys.contains(key(restriction.getTargetOptionCode(), restriction.getTargetValueCode())))) {
            return "product.formula.restrictionTargetInvalid";
        }
        if (StringUtils.isBlank(restriction.getConditionType()) || StringUtils.isBlank(restriction.getActionType())) {
            return "product.formula.restrictionConditionInvalid";
        }
        if ("EXPRESSION".equals(restriction.getConditionType())) {
            if (!ProductFormulaExpressionValidator.isConditionValid(restriction.getConditionExpression())) {
                return "product.formula.restrictionConditionInvalid";
            }
            String messageKey = context == null ? null : referenceValidator.validationMessageKey(restriction.getConditionExpression(),
                context.options(), context.values(), context.optionMaterials(), context.materials());
            return messageKey == null ? null : "product.formula.restrictionConditionInvalid";
        }
        if ("OPTION_VALUE".equals(restriction.getConditionType())) {
            String optionToken = optionToken(restriction.getConditionOptionRefKey(), restriction.getConditionOptionCode());
            String valueToken = valueToken(restriction.getConditionValueRefKey(), restriction.getConditionValueCode());
            if (StringUtils.isBlank(optionToken) || !optionCodes.contains(optionToken)) {
                return "product.formula.restrictionConditionInvalid";
            }
            if (StringUtils.isBlank(valueToken) || !valueKeys.contains(key(optionToken, valueToken))) {
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

    private String validateVariableRuleReferences(ProductFormulaSetupContext context) {
        for (ProductFormulaVariableRule rule : context.variableRules()) {
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || StringUtils.isBlank(rule.getConditionExpression())) {
                continue;
            }
            String messageKey = referenceValidator.validationMessageKey(rule.getConditionExpression(), context.options(),
                context.values(), context.optionMaterials(), context.materials());
            if (messageKey != null) {
                return "product.formula.variableRuleInvalid";
            }
        }
        return null;
    }

    private String validateUsageRuleOptionReferences(ProductFormulaSetupContext context, Set<String> optionCodes, Set<String> valueKeys) {
        for (ProductFormulaUsageRule rule : context.usageRules()) {
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
                continue;
            }
            if ("OPTION_VALUE".equals(rule.getConditionType())) {
                String optionToken = optionToken(rule.getConditionOptionRefKey(), rule.getConditionOptionCode());
                String valueToken = valueToken(rule.getConditionValueRefKey(), rule.getConditionValueCode());
                if (!optionCodes.contains(optionToken) || !valueKeys.contains(key(optionToken, valueToken))) {
                    return "product.formula.usageConditionInvalid";
                }
                continue;
            }
            if ("EXPRESSION".equals(rule.getConditionType())) {
                String messageKey = referenceValidator.validationMessageKey(rule.getConditionExpression(), context.options(),
                    context.values(), context.optionMaterials(), context.materials());
                if (messageKey != null) {
                    return "product.formula.usageConditionInvalid";
                }
            }
        }
        return null;
    }

    private String validateOptionVisibility(ProductFormulaOption option, Map<String, ProductFormulaOption> optionMap,
                                            Set<String> optionCodes, Set<String> valueKeys) {
        if (!VISIBILITY_CONDITIONAL.equals(option.getVisibilityMode())) {
            return null;
        }
        String conditionOption = optionToken(option.getVisibleConditionOptionRefKey(), option.getVisibleConditionOptionCode());
        String conditionValue = valueToken(option.getVisibleConditionValueRefKey(), option.getVisibleConditionValueCode());
        if (StringUtils.isBlank(conditionOption)) {
            return "product.formula.optionVisibilityConditionRequired";
        }
        if (conditionOption.equals(option.getOptionRefKey()) || conditionOption.equals(option.getOptionCode())) {
            return "product.formula.optionVisibilitySelfDenied";
        }
        if (!optionCodes.contains(conditionOption) && !optionMap.containsKey(option.getVisibleConditionOptionCode())) {
            return "product.formula.optionVisibilityConditionInvalid";
        }
        if (StringUtils.isBlank(conditionValue)) {
            return "product.formula.optionVisibilityValueRequired";
        }
        if (!valueKeys.contains(key(conditionOption, conditionValue))) {
            return "product.formula.optionVisibilityValueInvalid";
        }
        return null;
    }

    private boolean requiresLinkedMaterial(ProductFormulaOption option) {
        return option != null && SOURCE_MATERIAL_POOL.equals(option.getSourceType());
    }

    private Set<String> optionTokens(List<ProductFormulaOption> options) {
        return options.stream().flatMap(option -> java.util.stream.Stream.of(option.getOptionCode(), option.getOptionRefKey())).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
    }

    private Set<String> valueKeys(List<ProductFormulaOptionValue> values) {
        return values.stream().flatMap(value -> java.util.stream.Stream.of(
            key(optionToken(value.getOptionRefKey(), value.getOptionCode()), valueToken(value.getValueRefKey(), value.getValueCode())),
            key(value.getOptionCode(), value.getValueCode())
        )).collect(Collectors.toSet());
    }

    private String optionToken(String optionRefKey, String optionCode) {
        return StringUtils.isNotBlank(optionRefKey) ? optionRefKey : optionCode;
    }

    private String valueToken(String valueRefKey, String valueCode) {
        return StringUtils.isNotBlank(valueRefKey) ? valueRefKey : valueCode;
    }

    private String key(String... parts) {
        return String.join("|", parts);
    }
}
