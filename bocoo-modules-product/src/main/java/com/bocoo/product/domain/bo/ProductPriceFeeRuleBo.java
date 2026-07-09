package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductPriceFeeRule.class, reverseConvertGenerate = false)
@Schema(description = "附加费用规则业务对象")
public class ProductPriceFeeRuleBo extends BaseBo {
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
