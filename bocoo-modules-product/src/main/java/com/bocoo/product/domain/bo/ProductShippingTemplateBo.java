package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductShippingTemplate.class, reverseConvertGenerate = false)
@Schema(description = "邮费模板业务对象")
public class ProductShippingTemplateBo extends BaseBo {
    private Long shippingTemplateId;
    private Long tenantId;
    private String templateCode;
    private String templateName;
    private String currencyCode;
    private Boolean defaultFlag;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
    private List<ProductShippingTemplateRuleBo> rules = new ArrayList<>();
}
