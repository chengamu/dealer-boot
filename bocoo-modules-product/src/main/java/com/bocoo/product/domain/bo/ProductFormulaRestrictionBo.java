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
    private String targetOptionCode;
    private String conditionType;
    private String conditionOptionCode;
    private String conditionOperator;
    private String conditionValueCode;
    private BigDecimal conditionValueNumber;
    private String actionType;
    private String targetValueCode;
    private String messageText;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
