package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationItemVo;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
class ProductFormulaSimulationUsageCalculator {
    ProductFormulaSimulationItemVo buildItem(ProductFormulaMaterialVo material, ProductMaterial source,
                                             List<ProductFormulaUsageRuleVo> usageRules, Map<String, ProductUnit> unitMap,
                                             Map<String, Object> context) {
        ProductFormulaSimulationItemVo item = new ProductFormulaSimulationItemVo();
        item.setFormulaMaterialId(material.getFormulaMaterialId());
        item.setMaterialId(material.getMaterialId());
        item.setMaterialCode(material.getMaterialCode());
        item.setMaterialNameCn(material.getMaterialNameCn());
        item.setMaterialTypeNameCn(material.getMaterialTypeNameCn());
        item.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
        item.setSpecModelText(material.getSpecModelText());
        item.setLossRate(material.getLossRate());
        item.setProductionRemark(material.getProductionRemark());
        Map<String, Object> materialContext = materialContext(context, material);
        ProductFormulaUsageRuleVo matchedRule = matchUsageRule(usageRules, materialContext);
        String calculationUnitCode = calculationUnitCode(material, matchedRule, source);
        item.setUnitCode(calculationUnitCode);
        item.setUsageQty(resolveUsageQty(material, matchedRule, unitMap.get(calculationUnitCode), materialContext));
        item.setUsageSummary(resolveUsageSummary(item.getUsageQty(), matchedRule));
        if (source != null) {
            item.setUnitPrice(source.getUnitPrice());
            item.setSalesPrice(source.getSalesPrice());
        }
        BigDecimal price = Optional.ofNullable(item.getSalesPrice()).orElse(item.getUnitPrice());
        if (price != null && item.getUsageQty() != null) {
            item.setAmount(price.multiply(item.getUsageQty()).setScale(2, RoundingMode.HALF_UP));
        }
        return item;
    }
    private ProductFormulaUsageRuleVo matchUsageRule(List<ProductFormulaUsageRuleVo> usageRules, Map<String, Object> context) {
        List<ProductFormulaUsageRuleVo> sortedRules = usageRules.stream().filter(rule -> "ENABLED".equals(rule.getStatus()))
            .sorted(Comparator.comparing(ProductFormulaUsageRuleVo::getSortOrder, Comparator.nullsLast(Integer::compareTo))).toList();
        for (ProductFormulaUsageRuleVo rule : sortedRules) {
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
                continue;
            }
            if ("OPTION_VALUE".equals(rule.getConditionType())
                && optionValueRuleMatched(rule, context)) {
                return rule;
            }
            if ("EXPRESSION".equals(rule.getConditionType())
                && ProductFormulaExpressionValidator.evaluateCondition(rule.getConditionExpression(), context)) {
                return rule;
            }
        }
        return sortedRules.stream().filter(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag())
            || "DEFAULT".equals(rule.getConditionType())).findFirst().orElse(null);
    }

    private boolean optionValueRuleMatched(ProductFormulaUsageRuleVo rule, Map<String, Object> context) {
        String optionRef = StringUtils.blankToDefault(rule.getConditionOptionRefKey(), rule.getConditionOptionCode());
        String valueRef = StringUtils.blankToDefault(rule.getConditionValueRefKey(), rule.getConditionValueCode());
        if (StringUtils.isNotBlank(optionRef) && ProductFormulaSimulationSelections.selectedValueMatches(
            stringValue(context.get("option_" + identifierPart(optionRef))), valueRef)) {
            return true;
        }
        return ProductFormulaSimulationSelections.selectedValueMatches(
            stringValue(context.get("option_" + rule.getConditionOptionCode())), rule.getConditionValueCode());
    }

    private Map<String, Object> materialContext(Map<String, Object> context, ProductFormulaMaterialVo material) {
        Map<String, Object> result = new HashMap<>(context == null ? Map.of() : context);
        String groupCode = material.getAttributeGroupCode();
        if (StringUtils.isBlank(groupCode)) {
            return result;
        }
        result.put("material_" + groupCode + "_attributeGroup", groupCode);
        result.put("material_" + groupCode + "_materialType", material.getMaterialTypeCode());
        result.put("material_" + groupCode + "_materialCode", material.getMaterialCode());
        result.put("material_" + groupCode + "_materialName", material.getMaterialNameCn());
        if (material.getAttributeList() != null) {
            material.getAttributeList().forEach(attribute -> {
                if (StringUtils.isNotBlank(attribute.getAttributeCode())) {
                    result.put("material_" + identifierPart(groupCode) + "_" + identifierPart(attribute.getAttributeCode()), attributeValue(attribute));
                }
            });
        }
        return result;
    }

    private Object attributeValue(com.bocoo.product.domain.vo.ProductMaterialAttributeVo attribute) {
        if (attribute.getValueNumber() != null) {
            return attribute.getValueNumber();
        }
        if (attribute.getValueBool() != null) {
            return attribute.getValueBool();
        }
        return attribute.getValueText();
    }

    private String identifierPart(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private BigDecimal resolveUsageQty(ProductFormulaMaterialVo material, ProductFormulaUsageRuleVo rule, ProductUnit unit,
                                       Map<String, Object> context) {
        int scale = unit == null || unit.getPrecisionScale() == null ? 6 : unit.getPrecisionScale();
        RoundingMode roundingMode = roundingMode(rule == null ? null : rule.getRoundingMode(), material.getRoundingMode(), unit);
        if (rule == null) {
            return applyLimits(material.getFixedUsageQty() == null ? BigDecimal.ONE : material.getFixedUsageQty(),
                material.getMinUsageQty(), material.getMaxUsageQty(), scale, roundingMode);
        }
        BigDecimal usageQty = "FIXED".equals(rule.getUsageMode())
            ? Optional.ofNullable(rule.getFixedUsageQty()).orElse(BigDecimal.ONE)
            : formulaUsageQty(rule, context);
        return applyLimits(usageQty, rule.getMinUsageQty(), rule.getMaxUsageQty(), scale, roundingMode);
    }
    private BigDecimal formulaUsageQty(ProductFormulaUsageRuleVo rule, Map<String, Object> context) {
        if (StringUtils.isNotBlank(rule.getUsageFormula())) {
            return ProductFormulaExpressionValidator.evaluateFormula(rule.getUsageFormula(), context);
        }
        return multiplyPresent(evaluateFormula(rule.getLengthFormula(), context), evaluateFormula(rule.getWidthFormula(), context),
            evaluateFormula(rule.getHeightFormula(), context), evaluateFormula(rule.getWeightFormula(), context));
    }
    private BigDecimal evaluateFormula(String expression, Map<String, Object> context) {
        return StringUtils.isBlank(expression) ? null : ProductFormulaExpressionValidator.evaluateFormula(expression, context);
    }
    private BigDecimal multiplyPresent(BigDecimal... values) {
        BigDecimal result = null;
        for (BigDecimal value : values) {
            if (value != null) {
                result = result == null ? value : result.multiply(value);
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("usage formula required");
        }
        return result;
    }
    private BigDecimal applyLimits(BigDecimal usageQty, BigDecimal minUsageQty, BigDecimal maxUsageQty,
                                   int scale, RoundingMode roundingMode) {
        BigDecimal result = usageQty == null ? BigDecimal.ZERO : usageQty;
        if (minUsageQty != null && result.compareTo(minUsageQty) < 0) {
            result = minUsageQty;
        }
        if (maxUsageQty != null && result.compareTo(maxUsageQty) > 0) {
            result = maxUsageQty;
        }
        return result.setScale(scale, roundingMode);
    }

    private String calculationUnitCode(ProductFormulaMaterialVo material, ProductFormulaUsageRuleVo rule, ProductMaterial source) {
        if (rule != null && StringUtils.isNotBlank(rule.getCalculationUnitCode())) return rule.getCalculationUnitCode();
        if (StringUtils.isNotBlank(material.getCalculationUnitCode())) return material.getCalculationUnitCode();
        if (source != null && StringUtils.isNotBlank(source.getUnitCode())) return source.getUnitCode();
        return material.getUnitCode();
    }

    private RoundingMode roundingMode(String ruleMode, String materialMode, ProductUnit unit) {
        String value = StringUtils.blankToDefault(ruleMode,
            StringUtils.blankToDefault(materialMode, unit == null ? null : unit.getRoundingMode()));
        if (StringUtils.isBlank(value)) return RoundingMode.HALF_UP;
        try {
            return RoundingMode.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid rounding mode", ex);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String resolveUsageSummary(BigDecimal usageQty, ProductFormulaUsageRuleVo rule) {
        if (rule == null || "FIXED".equals(rule.getUsageMode())) {
            return usageQty == null ? "product.formula.simulation.fixedUsage" : usageQty.toPlainString();
        }
        return StringUtils.blankToDefault(rule.getRuleName(), "product.formula.simulation.formulaRules");
    }
}
