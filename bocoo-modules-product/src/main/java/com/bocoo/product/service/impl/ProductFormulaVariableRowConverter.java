package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.product.domain.bo.ProductFormulaVariableBo;
import com.bocoo.product.domain.bo.ProductFormulaVariableRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;

final class ProductFormulaVariableRowConverter {
    private ProductFormulaVariableRowConverter() {
    }

    static ProductFormulaVariable variable(ProductFormulaVariableBo source) {
        ProductFormulaVariable target = MapstructUtils.convert(source, ProductFormulaVariable.class);
        if (target != null) return target;
        target = new ProductFormulaVariable();
        target.setVariableId(source.getVariableId());
        target.setTenantId(source.getTenantId());
        target.setFormulaId(source.getFormulaId());
        target.setVariableCode(source.getVariableCode());
        target.setVariableName(source.getVariableName());
        target.setDelFlag(source.getDelFlag());
        target.setSortOrder(source.getSortOrder());
        target.setRemark(source.getRemark());
        return target;
    }

    static ProductFormulaVariableRule rule(ProductFormulaVariableRuleBo source) {
        ProductFormulaVariableRule target = MapstructUtils.convert(source, ProductFormulaVariableRule.class);
        if (target != null) return target;
        target = new ProductFormulaVariableRule();
        target.setRuleId(source.getRuleId());
        target.setTenantId(source.getTenantId());
        target.setFormulaId(source.getFormulaId());
        target.setVariableId(source.getVariableId());
        target.setVariableCode(source.getVariableCode());
        target.setConditionExpression(source.getConditionExpression());
        target.setConditionText(source.getConditionText());
        target.setValueType(source.getValueType());
        target.setFixedValue(source.getFixedValue());
        target.setFormulaExpression(source.getFormulaExpression());
        target.setFormulaText(source.getFormulaText());
        target.setDefaultRuleFlag(source.getDefaultRuleFlag());
        target.setDelFlag(source.getDelFlag());
        target.setSortOrder(source.getSortOrder());
        target.setRemark(source.getRemark());
        return target;
    }
}
