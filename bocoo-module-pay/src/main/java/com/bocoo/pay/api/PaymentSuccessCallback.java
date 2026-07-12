package com.bocoo.pay.api;

/**
 * Business callback invoked after a payment fact is confirmed.
 */
public interface PaymentSuccessCallback {

    void confirmPayment(PaymentSuccessCommand command);
}
