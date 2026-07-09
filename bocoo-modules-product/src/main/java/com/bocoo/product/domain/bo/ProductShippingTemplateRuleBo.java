package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductShippingTemplateRule.class, reverseConvertGenerate = false)
@Schema(description = "邮费模板规则业务对象")
public class ProductShippingTemplateRuleBo extends BaseBo {
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
