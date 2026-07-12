package com.bocoo.pay.paypal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayPalWebhookRequest {
    String eventId;
    String eventType;
    String paypalOrderId;
    String captureId;
    Long price;
    String currency;
    String transmissionId;
    String transmissionTime;
    String transmissionSig;
    String certUrl;
    String authAlgo;
    String rawBody;
}
