package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Payment order.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_order")
@Schema(description = "支付订单")
public class PayOrder extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "支付订单ID")
    private Long id;

    @Schema(description = "租户ID，默认等于付款方租户ID用于租户拦截")
    private Long tenantId;

    @Schema(description = "付款方商户租户ID")
    private Long payerTenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "支付应用ID")
    private Long appId;

    @Schema(description = "支付渠道ID")
    private Long channelId;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "支付订单号")
    private String no;

    @Schema(description = "业务订单号")
    private String merchantOrderId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "订单标题")
    private String subject;

    @Schema(description = "订单描述")
    private String body;

    @Schema(description = "业务通知地址")
    private String notifyUrl;

    @Schema(description = "支付金额，单位分")
    private Long price;

    @Schema(description = "已退款金额，单位分")
    private Long refundPrice;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "渠道手续费率，万分比")
    private Integer channelFeeRate;

    @Schema(description = "渠道手续费，单位分")
    private Long channelFeePrice;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "过期时间，UTC")
    private LocalDateTime expireTime;

    @Schema(description = "成功时间，UTC")
    private LocalDateTime successTime;

    @Schema(description = "当前支付提交记录ID")
    private Long extensionId;

    @Schema(description = "渠道用户ID")
    private String channelUserId;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;
}
