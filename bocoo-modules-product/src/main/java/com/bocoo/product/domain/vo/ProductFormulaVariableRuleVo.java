package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AutoMapper(target = ProductFormulaVariableRule.class)
public class ProductFormulaVariableRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private Long tenantId;
    private Long formulaId;
    private Long variableId;
    private String variableCode;
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
