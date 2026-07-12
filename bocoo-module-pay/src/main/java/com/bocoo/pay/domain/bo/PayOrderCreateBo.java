package com.bocoo.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Payment order creation command.
 */
@Data
@Schema(description = "支付订单创建参数")
public class PayOrderCreateBo {

    @Schema(description = "付款方商户租户ID")
    private Long payerTenantId;

    @Schema(description = "收款方平台/厂家租户ID")
    private Long payeeTenantId;

    @Schema(description = "支付应用标识")
    private String appKey;

    @Schema(description = "业务订单号")
    private String merchantOrderId;

    private Long salesDocumentId;
    private String salesOrderNo;
    private Long merchantId;
    private String merchantName;
    private Long customerId;
    private String customerName;

    @Schema(description = "订单标题")
    private String subject;

    @Schema(description = "订单描述")
    private String body;

    @Schema(description = "支付金额，单位分")
    private Long price;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "业务通知地址")
    private String notifyUrl;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户类型")
    private String userType;

    @Schema(description = "过期时间，UTC")
    private LocalDateTime expireTime;
}
