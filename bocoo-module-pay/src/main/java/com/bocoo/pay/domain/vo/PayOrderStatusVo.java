package com.bocoo.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Payment order status view.
 */
@Data
@Schema(description = "支付订单状态")
public class PayOrderStatusVo {

    @Schema(description = "支付订单ID")
    private Long orderId;

    @Schema(description = "支付订单号")
    private String orderNo;

    @Schema(description = "业务订单号")
    private String merchantOrderId;

    @Schema(description = "付款方商户租户ID")
    private Long payerTenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "支付渠道编码")
    private String channelCode;

    @Schema(description = "支付金额，单位分")
    private Long price;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "支付状态")
    private Integer status;

    @Schema(description = "成功时间，UTC")
    private LocalDateTime successTime;
}
