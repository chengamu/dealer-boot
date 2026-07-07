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
@TableName("merchant_level")
@Schema(description = "商户等级")
public class MerchantLevel extends BaseEntity {

    @TableId(value = "level_id")
    private Long levelId;

    private Long tenantId;

    private String levelCode;

    private String levelName;

    private BigDecimal defaultDiscountRate;

    private BigDecimal defaultCreditLimit;

    private Boolean defaultFlag;

    private Integer sortOrder;

    private String status;

    private String delFlag;

    private String remark;
}
