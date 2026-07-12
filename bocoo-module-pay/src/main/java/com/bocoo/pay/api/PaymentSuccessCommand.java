package com.bocoo.pay.api;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class PaymentSuccessCommand {
    Long payOrderId;
    String payOrderNo;
    String merchantOrderId;
    Long payerTenantId;
    String method;
    Long paidPrice;
    String currency;
    String channelOrderNo;
    LocalDateTime paidTime;
}
