package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_variable_rule")
@Schema(description = "配方内部变量取值规则")
public class ProductFormulaVariableRule extends BaseEntity {

    @TableId(value = "rule_id")
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
