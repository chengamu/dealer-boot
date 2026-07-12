package com.bocoo.pay.paypal;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
class PayPalAmountConverter {
    String major(Long price) {
        return BigDecimal.valueOf(price, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    Long minor(String value) {
        return new BigDecimal(value).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }
}
