package com.bocoo.pay.paypal;

import com.bocoo.common.core.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class PayPalSandboxCredentials {
    static final String BASE_URL = "https://api-m.sandbox.paypal.com";

    public String clientId() {
        return required("PAYPAL_CLIENT_ID");
    }

    public String clientSecret() {
        return required("PAYPAL_CLIENT_SECRET");
    }

    public String webhookId() {
        return required("PAYPAL_WEBHOOK_ID");
    }

    public String expectedMerchantId() {
        return System.getenv("PAYPAL_MERCHANT_ID");
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new ServiceException(name + " is not configured for PayPal Sandbox");
        }
        return value;
    }
}
