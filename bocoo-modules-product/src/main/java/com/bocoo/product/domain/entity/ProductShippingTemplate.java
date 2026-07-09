package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邮费模板 pc_shipping_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_shipping_template")
@Schema(description = "邮费模板")
public class ProductShippingTemplate extends BaseEntity {

    @TableId(value = "shipping_template_id")
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
}
