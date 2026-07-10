package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 邮费模板规则 pc_shipping_template_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_shipping_template_rule")
@Schema(description = "邮费模板规则")
public class ProductShippingTemplateRule extends BaseEntity {

    @TableId(value = "shipping_template_rule_id")
    private Long shippingTemplateRuleId;
    private Long tenantId;
    private Long shippingTemplateId;
    private String feeCode;
    private String feeName;
    private BigDecimal minAreaSqft;
    private BigDecimal maxAreaSqft;
    private BigDecimal feeAmount;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
