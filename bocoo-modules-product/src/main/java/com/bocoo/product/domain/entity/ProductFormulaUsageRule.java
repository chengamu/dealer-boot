package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配方条件用量规则 pc_formula_usage_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_usage_rule")
@Schema(description = "配方条件用量规则")
public class ProductFormulaUsageRule extends BaseEntity {

    @TableId(value = "usage_rule_id")
    private Long usageRuleId;

    private Long tenantId;
    private Long formulaId;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String ruleName;
    private String conditionType;
    private String conditionOptionCode;
    private String conditionOptionNameCn;
    private String conditionValueCode;
    private String conditionValueNameCn;
    private String conditionExpression;
    private String conditionText;
    private String conditionKey;
    private String usageMode;
    private BigDecimal fixedUsageQty;
    private String lengthFormula;
    private String lengthFormulaText;
    private String widthFormula;
    private String widthFormulaText;
    private String heightFormula;
    private String heightFormulaText;
    private String weightFormula;
    private String weightFormulaText;
    private String usageFormula;
    private String usageFormulaText;
    private String calculationUnitCode;
    private String roundingMode;
    private BigDecimal minUsageQty;
    private BigDecimal maxUsageQty;
    private BigDecimal lossRate;
    private Boolean defaultRuleFlag;
    private String productionRemark;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
