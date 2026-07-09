package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配方限制条件 pc_formula_restriction
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_restriction")
@Schema(description = "配方限制条件")
public class ProductFormulaRestriction extends BaseEntity {

    @TableId(value = "restriction_id")
    private Long restrictionId;

    private Long tenantId;
    private Long formulaId;
    private String restrictionName;
    private String targetOptionRefKey;
    private String targetOptionCode;
    private String conditionType;
    private String conditionOptionRefKey;
    private String conditionOptionCode;
    private String conditionOperator;
    private String conditionValueRefKey;
    private String conditionValueCode;
    private BigDecimal conditionValueNumber;
    private String conditionJson;
    private String conditionExpression;
    private String conditionText;
    private String actionType;
    private String targetValueRefKey;
    private String targetValueCode;
    private String messageText;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
