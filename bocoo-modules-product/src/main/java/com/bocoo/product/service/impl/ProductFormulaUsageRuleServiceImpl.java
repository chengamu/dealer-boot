package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaUsageRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import com.bocoo.product.mapper.ProductFormulaUsageRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFormulaUsageRuleServiceImpl extends ProductServiceSupport implements ProductFormulaUsageRuleService {

    private static final String CONDITION_DEFAULT = "DEFAULT";
    private static final String CONDITION_OPTION_VALUE = "OPTION_VALUE";
    private static final String CONDITION_EXPRESSION = "EXPRESSION";
    private static final String STATUS_ENABLED = ProductServiceSupport.STATUS_ENABLED;
    private static final String USAGE_FIXED = "FIXED";
    private static final String USAGE_FORMULA = "FORMULA";

    private final ProductFormulaUsageRuleMapper usageRuleMapper;

    @Override
    public List<ProductFormulaUsageRuleVo> queryByFormula(Long formulaId) {
        return usageRuleMapper.selectVoList(activeQuery(ProductFormulaUsageRule.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "usage_rule_id"));
    }

    @Override
    public List<ProductFormulaUsageRule> activeRules(Long formulaId) {
        return usageRuleMapper.selectList(activeQuery(ProductFormulaUsageRule.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "usage_rule_id"));
    }

    @Override
    public List<ProductFormulaUsageRule> normalize(Long formulaId, List<ProductFormulaMaterial> materials,
                                                   List<ProductFormulaOption> options, List<ProductFormulaOptionValue> values,
                                                   List<ProductFormulaUsageRuleBo> rows) {
        Map<Long, ProductFormulaMaterial> materialById = materials.stream()
            .filter(material -> material.getFormulaMaterialId() != null)
            .collect(Collectors.toMap(ProductFormulaMaterial::getFormulaMaterialId, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaMaterial> materialByCode = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaOption> optionMap = options.stream()
            .collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity(), (left, right) -> left));
        Map<String, ProductFormulaOptionValue> valueMap = values.stream()
            .collect(Collectors.toMap(value -> key(value.getOptionCode(), value.getValueCode()), Function.identity(), (left, right) -> left));
        Set<String> duplicateKeys = new HashSet<>();
        List<ProductFormulaUsageRule> result = new java.util.ArrayList<>();
        int index = 0;
        for (ProductFormulaUsageRuleBo row : rows == null ? List.<ProductFormulaUsageRuleBo>of() : rows) {
            result.add(normalizeRule(formulaId, row, materialById, materialByCode, optionMap, valueMap, duplicateKeys, index));
            index++;
        }
        String messageKey = validationMessageKey(materials, options, values, result);
        if (messageKey != null) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return result;
    }

    @Override
    public void insertAll(List<ProductFormulaUsageRule> rules, List<ProductFormulaMaterial> materials) {
        Map<String, Long> materialIds = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, ProductFormulaMaterial::getFormulaMaterialId, (left, right) -> left));
        for (ProductFormulaUsageRule rule : rules) {
            rule.setFormulaMaterialId(materialIds.get(rule.getMaterialCode()));
            ProductEntityDefaults.prepareInsert(rule);
            usageRuleMapper.insert(rule);
        }
    }

    @Override
    public void deleteByFormula(Long formulaId) {
        usageRuleMapper.delete(activeQuery(ProductFormulaUsageRule.class).eq("formula_id", formulaId));
    }

    @Override
    public String validationMessageKey(List<ProductFormulaMaterial> materials, List<ProductFormulaOption> options,
                                       List<ProductFormulaOptionValue> values, List<ProductFormulaUsageRule> usageRules) {
        Map<String, ProductFormulaMaterial> materialByCode = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, Function.identity(), (left, right) -> left));
        Set<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode).collect(Collectors.toSet());
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        Map<String, List<ProductFormulaUsageRule>> rulesByMaterial = usageRules.stream()
            .filter(rule -> STATUS_ENABLED.equals(rule.getStatus()))
            .collect(Collectors.groupingBy(ProductFormulaUsageRule::getMaterialCode));
        for (ProductFormulaUsageRule rule : usageRules) {
            String messageKey = validateRuleReference(rule, materialByCode, optionCodes, valueKeys);
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

    private ProductFormulaUsageRule normalizeRule(Long formulaId, ProductFormulaUsageRuleBo row,
                                                  Map<Long, ProductFormulaMaterial> materialById,
                                                  Map<String, ProductFormulaMaterial> materialByCode,
                                                  Map<String, ProductFormulaOption> optionMap,
                                                  Map<String, ProductFormulaOptionValue> valueMap,
                                                  Set<String> duplicateKeys, int index) {
        ProductFormulaUsageRule entity = MapstructUtils.convert(row, ProductFormulaUsageRule.class);
        if (entity == null) {
            throw ServiceException.ofMessageKey("product.formula.usageRuleRequired");
        }
        entity.setFormulaId(formulaId);
        entity.setMaterialCode(trim(entity.getMaterialCode()));
        ProductFormulaMaterial material = entity.getFormulaMaterialId() == null ? null : materialById.get(entity.getFormulaMaterialId());
        if (material == null && StringUtils.isNotBlank(entity.getMaterialCode())) {
            material = materialByCode.get(entity.getMaterialCode());
        }
        if (material == null) {
            throw ServiceException.ofMessageKey("product.formula.usageMaterialInvalid");
        }
        entity.setFormulaMaterialId(material.getFormulaMaterialId());
        entity.setMaterialId(material.getMaterialId());
        entity.setMaterialCode(material.getMaterialCode());
        entity.setMaterialNameCn(material.getMaterialNameCn());
        entity.setConditionType(defaultString(trimUpper(entity.getConditionType()), CONDITION_OPTION_VALUE));
        entity.setConditionOptionCode(trimUpper(entity.getConditionOptionCode()));
        entity.setConditionValueCode(trimUpper(entity.getConditionValueCode()));
        entity.setDefaultRuleFlag(Boolean.TRUE.equals(entity.getDefaultRuleFlag()));
        normalizeConditionSnapshot(entity, optionMap, valueMap);
        entity.setRuleName(defaultString(trim(entity.getRuleName()), defaultRuleName(entity)));
        entity.setUsageMode(defaultString(trimUpper(entity.getUsageMode()), USAGE_FIXED));
        entity.setLengthFormula(trim(entity.getLengthFormula()));
        entity.setLengthFormulaText(defaultString(trim(entity.getLengthFormulaText()), entity.getLengthFormula()));
        entity.setWidthFormula(trim(entity.getWidthFormula()));
        entity.setWidthFormulaText(defaultString(trim(entity.getWidthFormulaText()), entity.getWidthFormula()));
        entity.setHeightFormula(trim(entity.getHeightFormula()));
        entity.setHeightFormulaText(defaultString(trim(entity.getHeightFormulaText()), entity.getHeightFormula()));
        entity.setWeightFormula(trim(entity.getWeightFormula()));
        entity.setWeightFormulaText(defaultString(trim(entity.getWeightFormulaText()), entity.getWeightFormula()));
        entity.setUsageFormula(trim(entity.getUsageFormula()));
        entity.setUsageFormulaText(defaultString(trim(entity.getUsageFormulaText()), entity.getUsageFormula()));
        entity.setCalculationUnitCode(defaultString(trim(entity.getCalculationUnitCode()), material.getCalculationUnitCode()));
        entity.setRoundingMode(trimUpper(entity.getRoundingMode()));
        entity.setProductionRemark(trim(entity.getProductionRemark()));
        entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
        entity.setDelFlag("0");
        entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
        String messageKey = validateRuleContent(entity);
        if (messageKey != null) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        String duplicateKey = key(entity.getMaterialCode(), entity.getConditionKey());
        if (STATUS_ENABLED.equals(entity.getStatus()) && !duplicateKeys.add(duplicateKey)) {
            throw ServiceException.ofMessageKey("product.formula.usageRuleDuplicate");
        }
        return entity;
    }

    private void normalizeConditionSnapshot(ProductFormulaUsageRule entity, Map<String, ProductFormulaOption> optionMap,
                                            Map<String, ProductFormulaOptionValue> valueMap) {
        if (Boolean.TRUE.equals(entity.getDefaultRuleFlag()) || CONDITION_DEFAULT.equals(entity.getConditionType())) {
            entity.setConditionType(CONDITION_DEFAULT);
            entity.setDefaultRuleFlag(Boolean.TRUE);
            entity.setConditionOptionCode(null);
            entity.setConditionOptionNameCn(null);
            entity.setConditionValueCode(null);
            entity.setConditionValueNameCn(null);
            entity.setConditionExpression(CONDITION_DEFAULT);
            entity.setConditionText("默认规则");
            entity.setConditionKey(CONDITION_DEFAULT);
            return;
        }
        if (CONDITION_EXPRESSION.equals(entity.getConditionType())) {
            entity.setConditionExpression(trim(entity.getConditionExpression()));
            entity.setConditionText(defaultString(trim(entity.getConditionText()), entity.getConditionExpression()));
            if (!ProductFormulaExpressionValidator.isConditionValid(entity.getConditionExpression())) {
                throw ServiceException.ofMessageKey("product.formula.usageConditionInvalid");
            }
            entity.setConditionKey(defaultString(trim(entity.getConditionKey()), "EXPR:" + entity.getConditionExpression()));
            return;
        }
        if (!CONDITION_OPTION_VALUE.equals(entity.getConditionType())) {
            throw ServiceException.ofMessageKey("product.formula.usageConditionInvalid");
        }
        ProductFormulaOption option = optionMap.get(entity.getConditionOptionCode());
        ProductFormulaOptionValue value = valueMap.get(key(entity.getConditionOptionCode(), entity.getConditionValueCode()));
        if (option == null || value == null) {
            throw ServiceException.ofMessageKey("product.formula.usageConditionInvalid");
        }
        entity.setConditionOptionNameCn(option.getOptionNameCn());
        entity.setConditionValueNameCn(value.getValueNameCn());
        entity.setConditionExpression(defaultString(trim(entity.getConditionExpression()), optionConditionExpression(option.getOptionCode(), value.getValueCode())));
        entity.setConditionText(defaultString(trim(entity.getConditionText()), option.getOptionNameCn() + " = " + value.getValueNameCn()));
        entity.setConditionKey(defaultString(trim(entity.getConditionKey()), "OPTION:" + option.getOptionCode() + ":" + value.getValueCode()));
        if (!ProductFormulaExpressionValidator.isConditionValid(entity.getConditionExpression())) {
            throw ServiceException.ofMessageKey("product.formula.usageConditionInvalid");
        }
    }

    private String validateRuleReference(ProductFormulaUsageRule rule, Map<String, ProductFormulaMaterial> materialByCode,
                                         Set<String> optionCodes, Set<String> valueKeys) {
        if (!materialByCode.containsKey(rule.getMaterialCode())) {
            return "product.formula.usageMaterialInvalid";
        }
        if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || CONDITION_DEFAULT.equals(rule.getConditionType())) {
            return null;
        }
        if (!CONDITION_OPTION_VALUE.equals(rule.getConditionType())) {
            return CONDITION_EXPRESSION.equals(rule.getConditionType())
                && ProductFormulaExpressionValidator.isConditionValid(rule.getConditionExpression())
                ? null : "product.formula.usageConditionInvalid";
        }
        if (!optionCodes.contains(rule.getConditionOptionCode())
            || !valueKeys.contains(key(rule.getConditionOptionCode(), rule.getConditionValueCode()))) {
            return "product.formula.usageConditionInvalid";
        }
        return null;
    }

    private String validateRuleContent(ProductFormulaUsageRule rule) {
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

    private String defaultRuleName(ProductFormulaUsageRule entity) {
        if (Boolean.TRUE.equals(entity.getDefaultRuleFlag()) || CONDITION_DEFAULT.equals(entity.getConditionType())) {
            return "默认规则";
        }
        return defaultString(entity.getConditionValueNameCn(), "条件规则");
    }

    private String optionConditionExpression(String optionCode, String valueCode) {
        String variableName = "FABRIC".equals(optionCode) ? "fabric" : "option_" + optionCode;
        return variableName + " == \"" + valueCode + "\"";
    }

    private String trimUpper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private String key(String... parts) {
        return java.util.Arrays.stream(parts)
            .map(part -> part == null ? "" : part)
            .collect(Collectors.joining("|"));
    }
}
