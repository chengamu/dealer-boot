package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaUsageRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import com.bocoo.product.mapper.ProductFormulaUsageRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    private final ProductFormulaUsageRuleMapper usageRuleMapper;
    private final ProductFormulaUsageRuleValidator usageRuleValidator;

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
                                                   List<ProductFormulaOptionMaterial> optionMaterials, List<ProductFormulaUsageRuleBo> rows) {
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
        String messageKey = usageRuleValidator.validationMessageKey(materials, options, values, optionMaterials, result);
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
                                       List<ProductFormulaOptionValue> values, List<ProductFormulaOptionMaterial> optionMaterials,
                                       List<ProductFormulaUsageRule> usageRules) {
        return usageRuleValidator.validationMessageKey(materials, options, values, optionMaterials, usageRules);
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
        String messageKey = usageRuleValidator.validateRuleContent(entity);
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
