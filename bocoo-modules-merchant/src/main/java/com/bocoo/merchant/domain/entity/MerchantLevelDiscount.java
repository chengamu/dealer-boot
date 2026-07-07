package com.bocoo.merchant.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_level_discount")
@Schema(description = "商户等级产品折扣")
public class MerchantLevelDiscount extends BaseEntity {

    @TableId(value = "discount_id")
    private Long discountId;

    private Long tenantId;

    private Long levelId;

    private String levelCode;

    private String levelName;

    private Long categoryId;

    private String categoryCode;

    private String categoryNameCn;

    private String productTypeCode;

    private String productTypeNameCn;

    private BigDecimal discountRate;

    private Integer sortOrder;

    private String status;

    private String delFlag;

    private String remark;
}
