package com.bocoo.pay.paypal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayPalCreateRequest {
    String requestId;
    String invoiceId;
    String customId;
    String description;
    Long price;
    String currency;
}
