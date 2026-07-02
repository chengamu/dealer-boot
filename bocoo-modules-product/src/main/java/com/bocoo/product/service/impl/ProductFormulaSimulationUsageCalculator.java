package com.bocoo.product.service.impl;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductMaterial;
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
import java.util.Objects;
import java.util.Optional;
@Component
class ProductFormulaSimulationUsageCalculator {
    ProductFormulaSimulationItemVo buildItem(ProductFormulaMaterialVo material, ProductMaterial source,
                                             List<ProductFormulaUsageRuleVo> usageRules, Map<String, Object> context) {
        ProductFormulaSimulationItemVo item = new ProductFormulaSimulationItemVo();
        item.setFormulaMaterialId(material.getFormulaMaterialId());
        item.setMaterialId(material.getMaterialId());
        item.setMaterialCode(material.getMaterialCode());
        item.setMaterialNameCn(material.getMaterialNameCn());
        item.setMaterialTypeNameCn(material.getMaterialTypeNameCn());
        item.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
        item.setSpecModelText(material.getSpecModelText());
        item.setUnitCode(material.getUnitCode());
        item.setLossRate(material.getLossRate());
        item.setProductionRemark(material.getProductionRemark());
        Map<String, Object> materialContext = materialContext(context, material);
        ProductFormulaUsageRuleVo matchedRule = matchUsageRule(usageRules, materialContext);
        item.setUsageQty(resolveUsageQty(material, matchedRule, materialContext));
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
        List<ProductFormulaUsageRuleVo> sortedRules = usageRules.stream().filter(rule -> !"DISABLED".equals(rule.getStatus()))
            .sorted(Comparator.comparing(ProductFormulaUsageRuleVo::getSortOrder, Comparator.nullsLast(Integer::compareTo))).toList();
        for (ProductFormulaUsageRuleVo rule : sortedRules) {
            if (Boolean.TRUE.equals(rule.getDefaultRuleFlag()) || "DEFAULT".equals(rule.getConditionType())) {
                continue;
            }
            if ("OPTION_VALUE".equals(rule.getConditionType())
                && Objects.equals(context.get("option_" + rule.getConditionOptionCode()), rule.getConditionValueCode())) {
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
        return result;
    }

    private BigDecimal resolveUsageQty(ProductFormulaMaterialVo material, ProductFormulaUsageRuleVo rule, Map<String, Object> context) {
        if (rule == null) {
            return applyLimitsAndLoss(material.getFixedUsageQty() == null ? BigDecimal.ONE : material.getFixedUsageQty(),
                material.getMinUsageQty(), material.getMaxUsageQty(), material.getLossRate());
        }
        BigDecimal usageQty = "FIXED".equals(rule.getUsageMode())
            ? Optional.ofNullable(rule.getFixedUsageQty()).orElse(BigDecimal.ONE)
            : formulaUsageQty(rule, context);
        return applyLimitsAndLoss(usageQty, rule.getMinUsageQty(), rule.getMaxUsageQty(), rule.getLossRate());
    }
    private BigDecimal formulaUsageQty(ProductFormulaUsageRuleVo rule, Map<String, Object> context) {
        if (StringUtils.isNotBlank(rule.getUsageFormula())) {
            return BigDecimal.valueOf(ProductFormulaExpressionValidator.evaluateFormula(rule.getUsageFormula(), context));
        }
        return multiplyPresent(evaluateFormula(rule.getLengthFormula(), context), evaluateFormula(rule.getWidthFormula(), context),
            evaluateFormula(rule.getHeightFormula(), context), evaluateFormula(rule.getWeightFormula(), context));
    }
    private BigDecimal evaluateFormula(String expression, Map<String, Object> context) {
        return StringUtils.isBlank(expression) ? null : BigDecimal.valueOf(ProductFormulaExpressionValidator.evaluateFormula(expression, context));
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
    private BigDecimal applyLimitsAndLoss(BigDecimal usageQty, BigDecimal minUsageQty, BigDecimal maxUsageQty, BigDecimal lossRate) {
        BigDecimal result = usageQty == null ? BigDecimal.ZERO : usageQty;
        if (lossRate != null && lossRate.compareTo(BigDecimal.ZERO) > 0) {
            result = result.multiply(BigDecimal.ONE.add(lossRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)));
        }
        if (minUsageQty != null && result.compareTo(minUsageQty) < 0) {
            result = minUsageQty;
        }
        if (maxUsageQty != null && result.compareTo(maxUsageQty) > 0) {
            result = maxUsageQty;
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }
    private String resolveUsageSummary(BigDecimal usageQty, ProductFormulaUsageRuleVo rule) {
        if (rule == null || "FIXED".equals(rule.getUsageMode())) {
            return usageQty == null ? "product.formula.simulation.fixedUsage" : usageQty.toPlainString();
        }
        return StringUtils.blankToDefault(rule.getRuleName(), "product.formula.simulation.formulaRules");
    }
}
