package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.bo.ProductFormulaVariableBo;
import com.bocoo.product.domain.bo.ProductFormulaVariableRuleBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableRuleVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaUsageRuleMapper;
import com.bocoo.product.mapper.ProductFormulaVariableMapper;
import com.bocoo.product.mapper.ProductFormulaVariableRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFormulaVariableServiceImpl extends ProductServiceSupport implements ProductFormulaVariableService {
    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String VALUE_FIXED = "FIXED";
    private static final String VALUE_FORMULA = "FORMULA";

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaVariableMapper variableMapper;
    private final ProductFormulaVariableRuleMapper ruleMapper;
    private final ProductFormulaUsageRuleMapper usageRuleMapper;
    private final ProductFormulaVariableReferenceValidator referenceValidator = new ProductFormulaVariableReferenceValidator();

    @Override
    public ProductFormulaSetupVo queryVariables(Long formulaId) {
        ProductFormulaSetupVo vo = new ProductFormulaSetupVo();
        vo.setFormula(formulaMapper.selectVoById(formulaId));
        vo.setVariables(queryVariableVos(formulaId));
        vo.setVariableRules(queryRuleVos(formulaId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveVariables(Long formulaId, ProductFormulaSetupBo bo) {
        requireEditableFormula(formulaId);
        ProductFormulaSetupBo safeBo = bo == null ? new ProductFormulaSetupBo() : bo;
        List<ProductFormulaVariable> variables = normalizeVariables(formulaId, safeBo.getVariables());
        List<ProductFormulaVariableRule> rules = normalizeRules(formulaId, variables, safeBo.getVariableRules());
        assertRemovedVariablesNotReferenced(formulaId, variables);
        replace(formulaId, variables, rules);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copyFrom(Long formulaId, String sourceFormulaCode) {
        requireEditableFormula(formulaId);
        String code = requiredTrim(sourceFormulaCode, "product.formula.codeRequired");
        ProductFormula sourceFormula = formulaMapper.selectOne(activeQuery(ProductFormula.class)
            .eq("formula_code", code));
        if (sourceFormula == null) {
            throw ServiceException.ofMessageKey("product.formula.copySourceNotFound");
        }
        Long sourceFormulaId = sourceFormula.getFormulaId();
        List<ProductFormulaVariable> variables = activeVariables(sourceFormulaId).stream().map(this::copyVariable).toList();
        List<ProductFormulaVariableRule> rules = activeRules(sourceFormulaId).stream().map(this::copyRule).toList();
        replace(formulaId, variables.stream().peek(row -> row.setFormulaId(formulaId)).toList(),
            rules.stream().peek(row -> row.setFormulaId(formulaId)).toList());
        return Boolean.TRUE;
    }

    @Override
    public Boolean validateVariables(Long formulaId) {
        String messageKey = validationMessageKey(activeVariables(formulaId), activeRules(formulaId), activeUsageRules(formulaId));
        if (messageKey != null) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<ProductFormulaVariableVo> queryVariableVos(Long formulaId) {
        return variableMapper.selectVoList(activeQuery(ProductFormulaVariable.class).eq("formula_id", formulaId)
            .orderByAsc("sort_order", "variable_id"));
    }

    @Override
    public List<ProductFormulaVariableRuleVo> queryRuleVos(Long formulaId) {
        return ruleMapper.selectVoList(activeQuery(ProductFormulaVariableRule.class).eq("formula_id", formulaId)
            .orderByAsc("sort_order", "rule_id"));
    }

    @Override
    public List<ProductFormulaVariable> activeVariables(Long formulaId) {
        return variableMapper.selectList(activeQuery(ProductFormulaVariable.class).eq("formula_id", formulaId)
            .orderByAsc("sort_order", "variable_id"));
    }

    @Override
    public List<ProductFormulaVariableRule> activeRules(Long formulaId) {
        return ruleMapper.selectList(activeQuery(ProductFormulaVariableRule.class).eq("formula_id", formulaId)
            .orderByAsc("sort_order", "rule_id"));
    }

    @Override
    public List<ProductFormulaVariable> normalizeVariables(Long formulaId, List<ProductFormulaVariableBo> rows) {
        List<ProductFormulaVariable> result = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        int index = 0;
        for (ProductFormulaVariableBo row : rows == null ? List.<ProductFormulaVariableBo>of() : rows) {
            if (row == null) continue;
            ProductFormulaVariable entity = ProductFormulaVariableRowConverter.variable(row);
            entity.setFormulaId(formulaId);
            entity.setVariableCode(requiredUpper(entity.getVariableCode(), "product.formula.variableCodeRequired"));
            entity.setVariableName(requiredTrim(entity.getVariableName(), "product.formula.variableNameRequired"));
            if (!codes.add(entity.getVariableCode())) throw ServiceException.ofMessageKey("product.formula.variableCodeDuplicate");
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }

    @Override
    public List<ProductFormulaVariableRule> normalizeRules(Long formulaId, List<ProductFormulaVariable> variables,
                                                           List<ProductFormulaVariableRuleBo> rows) {
        Set<String> codes = variables.stream().map(ProductFormulaVariable::getVariableCode).collect(Collectors.toSet());
        List<ProductFormulaVariableRule> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaVariableRuleBo row : rows == null ? List.<ProductFormulaVariableRuleBo>of() : rows) {
            if (row == null) continue;
            ProductFormulaVariableRule entity = ProductFormulaVariableRowConverter.rule(row);
            entity.setFormulaId(formulaId);
            entity.setVariableCode(requiredUpper(entity.getVariableCode(), "product.formula.variableCodeRequired"));
            if (!codes.contains(entity.getVariableCode())) throw ServiceException.ofMessageKey("product.formula.variableRuleInvalid");
            normalizeRuleContent(entity);
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        String messageKey = validationMessageKey(variables, result, List.of());
        if (messageKey != null) throw ServiceException.ofMessageKey(messageKey);
        return result;
    }

    @Override
    public void replace(Long formulaId, List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules) {
        ruleMapper.delete(activeQuery(ProductFormulaVariableRule.class).eq("formula_id", formulaId));
        variableMapper.delete(activeQuery(ProductFormulaVariable.class).eq("formula_id", formulaId));
        insertAll(variables, rules);
    }

    @Override
    public void insertAll(List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (ProductFormulaVariable variable : variables) {
            ProductEntityDefaults.prepareInsert(variable);
            variableMapper.insert(variable);
            ids.put(variable.getVariableCode(), variable.getVariableId());
        }
        for (ProductFormulaVariableRule rule : rules) {
            rule.setVariableId(ids.get(rule.getVariableCode()));
            ProductEntityDefaults.prepareInsert(rule);
            ruleMapper.insert(rule);
        }
    }

    @Override
    public String validationMessageKey(List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules,
                                       List<ProductFormulaUsageRule> usageRules) {
        return referenceValidator.validationMessageKey(variables, rules, usageRules);
    }

    @Override
    public Map<String, Object> evaluateVariables(List<ProductFormulaVariableVo> variables, List<ProductFormulaVariableRuleVo> rules,
                                                 Map<String, Object> context) {
        Map<String, Object> result = new LinkedHashMap<>(context == null ? Map.of() : context);
        Map<String, List<ProductFormulaVariableRuleVo>> rulesByCode = rules.stream()
            .collect(Collectors.groupingBy(ProductFormulaVariableRuleVo::getVariableCode));
        variables.stream().sorted(Comparator.comparing(ProductFormulaVariableVo::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
            .forEach(variable -> result.put(ProductFormulaExpressionValidator.variableName(variable.getVariableCode()),
                evaluateVariableRule(rulesByCode.getOrDefault(variable.getVariableCode(), List.of()), result)));
        return result;
    }

    private void normalizeRuleContent(ProductFormulaVariableRule entity) {
        entity.setValueType(defaultString(trimUpper(entity.getValueType()), VALUE_FIXED));
        entity.setConditionExpression(trim(entity.getConditionExpression()));
        entity.setConditionText(defaultString(trim(entity.getConditionText()), entity.getConditionExpression()));
        entity.setDefaultRuleFlag(Boolean.TRUE.equals(entity.getDefaultRuleFlag()) || StringUtils.isBlank(entity.getConditionExpression()));
        if (!Boolean.TRUE.equals(entity.getDefaultRuleFlag()) && !ProductFormulaExpressionValidator.isConditionValid(entity.getConditionExpression())) {
            throw ServiceException.ofMessageKey("product.formula.variableRuleInvalid");
        }
        if (VALUE_FORMULA.equals(entity.getValueType())) {
            entity.setFormulaExpression(requiredTrim(entity.getFormulaExpression(), "product.formula.variableFormulaRequired"));
            entity.setFormulaText(defaultString(trim(entity.getFormulaText()), entity.getFormulaExpression()));
            if (!ProductFormulaExpressionValidator.isFormulaValid(entity.getFormulaExpression())) {
                throw ServiceException.ofMessageKey("product.formula.variableFormulaInvalid");
            }
            entity.setFixedValue(null);
        } else {
            entity.setValueType(VALUE_FIXED);
            if (entity.getFixedValue() == null) throw ServiceException.ofMessageKey("product.formula.variableFixedValueRequired");
            entity.setFormulaExpression(null);
            entity.setFormulaText(null);
        }
    }

    private Object evaluateVariableRule(List<ProductFormulaVariableRuleVo> rules, Map<String, Object> context) {
        ProductFormulaVariableRuleVo rule = rules.stream().filter(item -> !Boolean.TRUE.equals(item.getDefaultRuleFlag()))
            .filter(item -> ProductFormulaExpressionValidator.evaluateCondition(item.getConditionExpression(), context)).findFirst()
            .orElseGet(() -> rules.stream().filter(item -> Boolean.TRUE.equals(item.getDefaultRuleFlag())).findFirst().orElse(null));
        if (rule == null) throw new IllegalArgumentException("variable rule required");
        if (VALUE_FORMULA.equals(rule.getValueType())) {
            return ProductFormulaExpressionValidator.evaluateFormula(rule.getFormulaExpression(), context);
        }
        return rule.getFixedValue() == null ? 0D : rule.getFixedValue().doubleValue();
    }

    private void assertRemovedVariablesNotReferenced(Long formulaId, List<ProductFormulaVariable> variables) {
        Set<String> newCodes = variables.stream().map(ProductFormulaVariable::getVariableCode).collect(Collectors.toSet());
        Set<String> removed = activeVariables(formulaId).stream().map(ProductFormulaVariable::getVariableCode)
            .filter(code -> !newCodes.contains(code)).collect(Collectors.toSet());
        if (!removed.isEmpty() && !referenceValidator.referencedUsageVariableCodes(activeUsageRules(formulaId)).stream().filter(removed::contains).toList().isEmpty()) {
            throw ServiceException.ofMessageKey("product.formula.variableReferencedRemoveDenied");
        }
    }

    private List<ProductFormulaUsageRule> activeUsageRules(Long formulaId) {
        return usageRuleMapper.selectList(activeQuery(ProductFormulaUsageRule.class).eq("formula_id", formulaId));
    }

    private ProductFormula requireEditableFormula(Long formulaId) {
        ProductFormula formula = formulaId == null ? null : formulaMapper.selectById(formulaId);
        if (formula == null) throw ServiceException.ofMessageKey("product.base.edit.notFound");
        if (!STATUS_DRAFT.equals(formula.getStatus()) && !STATUS_REJECTED.equals(formula.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.editDenied");
        }
        return formula;
    }

    private ProductFormulaVariable copyVariable(ProductFormulaVariable source) {
        ProductFormulaVariable target = MapstructUtils.convert(source, ProductFormulaVariable.class);
        target.setVariableId(null);
        return target;
    }

    private ProductFormulaVariableRule copyRule(ProductFormulaVariableRule source) {
        ProductFormulaVariableRule target = MapstructUtils.convert(source, ProductFormulaVariableRule.class);
        target.setRuleId(null);
        target.setVariableId(null);
        return target;
    }

    private String requiredTrim(String value, String messageKey) {
        String trimmed = trim(value);
        if (StringUtils.isBlank(trimmed)) throw ServiceException.ofMessageKey(messageKey);
        return trimmed;
    }

    private String requiredUpper(String value, String messageKey) {
        return requiredTrim(value, messageKey).toUpperCase(Locale.ROOT);
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

}
