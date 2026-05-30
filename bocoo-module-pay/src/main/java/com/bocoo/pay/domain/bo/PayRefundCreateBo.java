package com.bocoo.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Payment refund creation command.
 */
@Data
@Schema(description = "支付退款创建参数")
public class PayRefundCreateBo {

    @Schema(description = "支付订单ID")
    private Long orderId;

    @Schema(description = "业务退款号")
    private String merchantRefundId;

    @Schema(description = "退款金额，单位分")
    private Long refundPrice;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "业务通知地址")
    private String notifyUrl;

    @Schema(description = "用户IP")
    private String userIp;
}
