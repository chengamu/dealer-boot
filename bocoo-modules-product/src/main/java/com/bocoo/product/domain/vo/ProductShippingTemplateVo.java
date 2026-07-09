package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductShippingTemplate;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AutoMapper(target = ProductShippingTemplate.class)
public class ProductShippingTemplateVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer ruleCount;
    private List<ProductShippingTemplateRuleVo> rules = new ArrayList<>();
}
