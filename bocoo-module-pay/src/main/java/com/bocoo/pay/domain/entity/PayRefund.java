package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Payment refund.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_refund")
@Schema(description = "支付退款")
public class PayRefund extends BaseEntity {

    @TableId(value = "id")
    @Schema(description = "退款ID")
    private Long id;

    @Schema(description = "租户ID，默认等于付款方租户ID用于租户拦截")
    private Long tenantId;

    @Schema(description = "付款方商户租户ID")
    private Long payerTenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "退款单号")
    private String no;

    @Schema(description = "支付应用ID")
    private Long appId;

    @Schema(description = "支付渠道ID")
    private Long channelId;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "支付订单ID")
    private Long orderId;

    @Schema(description = "支付订单号")
    private String orderNo;

    @Schema(description = "业务订单号")
    private String merchantOrderId;

    @Schema(description = "业务退款号")
    private String merchantRefundId;

    @Schema(description = "业务通知地址")
    private String notifyUrl;

    @Schema(description = "退款状态")
    private Integer status;

    @Schema(description = "支付金额，单位分")
    private Long payPrice;

    @Schema(description = "退款金额，单位分")
    private Long refundPrice;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "渠道订单号")
    private String channelOrderNo;

    @Schema(description = "渠道退款号")
    private String channelRefundNo;

    @Schema(description = "退款成功时间，UTC")
    private LocalDateTime successTime;

    @Schema(description = "渠道错误码")
    private String channelErrorCode;

    @Schema(description = "渠道错误信息")
    private String channelErrorMsg;

    @Schema(description = "渠道回调原始数据")
    private String channelNotifyData;
}
