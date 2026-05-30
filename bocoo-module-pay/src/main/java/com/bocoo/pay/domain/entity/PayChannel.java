package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Payment channel configuration.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_channel")
@Schema(description = "支付渠道配置")
public class PayChannel extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "渠道ID")
    private Long id;

    @Schema(description = "租户ID，当前为收款方平台/厂家租户")
    private Long tenantId;

    @Schema(description = "渠道编码")
    private String code;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "手续费率，万分比")
    private Integer feeRate;

    @Schema(description = "支付应用ID")
    private Long appId;

    @Schema(description = "渠道配置JSON，输出前必须脱敏")
    private String config;

    @Schema(description = "备注")
    private String remark;
}
