package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductFormulaRestriction.class)
public class ProductFormulaRestrictionVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
