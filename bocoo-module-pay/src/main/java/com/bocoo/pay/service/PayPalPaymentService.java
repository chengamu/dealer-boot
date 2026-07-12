package com.bocoo.pay.service;

import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.paypal.PayPalWebhookRequest;

public interface PayPalPaymentService {
    PayPalCheckoutVo create(Long payOrderId);

    PayPalCheckoutVo capture(Long payOrderId, String paypalOrderId);

    PayPalCheckoutVo reconcile(Long payOrderId);

    boolean handleWebhook(PayPalWebhookRequest request);
}
