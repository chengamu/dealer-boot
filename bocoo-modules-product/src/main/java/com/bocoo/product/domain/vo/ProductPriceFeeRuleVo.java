package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AutoMapper(target = ProductPriceFeeRule.class)
public class ProductPriceFeeRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long feeRuleId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long shippingTemplateId;
    private String shippingTemplateCode;
    private String shippingTemplateName;
    private Long shippingTemplateRuleId;
    private String feeCode;
    private String feeName;
    private String feeCategory;
    private String triggerCondition;
    private String feeMode;
    private BigDecimal feeAmount;
    private BigDecimal minAreaSqft;
    private BigDecimal maxAreaSqft;
    private String formulaText;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
