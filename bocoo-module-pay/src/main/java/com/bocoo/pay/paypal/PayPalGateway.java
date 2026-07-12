package com.bocoo.pay.paypal;

public interface PayPalGateway {
    PayPalOrderResult createOrder(PayPalCreateRequest request);

    PayPalOrderResult captureOrder(String paypalOrderId, String requestId);

    PayPalOrderResult getOrder(String paypalOrderId);

    boolean verifyWebhook(PayPalWebhookRequest request);
}
