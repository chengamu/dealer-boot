package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaRestriction.class, reverseConvertGenerate = false)
public class ProductFormulaRestrictionBo extends BaseBo {
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
