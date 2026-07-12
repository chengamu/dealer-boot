package com.bocoo.pay.paypal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayPalOrderResult {
    String orderId;
    String captureId;
    String status;
    Long price;
    String currency;
    String payeeMerchantId;
    String approvalUrl;

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }
}
