package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaVariableRule.class, reverseConvertGenerate = false)
public class ProductFormulaVariableRuleBo extends BaseBo {
    private Long ruleId;
    private Long tenantId;
    private Long formulaId;
    private Long variableId;
    private String variableKey;
    private String variableCode;
    private String conditionJson;
    private String conditionExpression;
    private String conditionText;
    private String valueType;
    private BigDecimal fixedValue;
    private String formulaExpression;
    private String formulaText;
    private Boolean defaultRuleFlag;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
