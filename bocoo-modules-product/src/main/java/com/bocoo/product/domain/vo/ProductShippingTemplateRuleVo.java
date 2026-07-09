package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AutoMapper(target = ProductShippingTemplateRule.class)
public class ProductShippingTemplateRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long shippingTemplateRuleId;
    private Long tenantId;
    private Long shippingTemplateId;
    private String feeCode;
    private String feeName;
    private BigDecimal minAreaSqft;
    private BigDecimal maxAreaSqft;
    private String formulaText;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
