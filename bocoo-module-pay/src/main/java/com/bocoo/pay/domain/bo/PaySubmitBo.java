package com.bocoo.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Payment submission command.
 */
@Data
@Schema(description = "提交支付参数")
public class PaySubmitBo {

    @Schema(description = "支付订单ID")
    private Long orderId;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "渠道扩展参数JSON")
    private String channelExtras;
}
