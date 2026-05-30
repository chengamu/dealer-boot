package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wallet transaction business types.
 */
@Getter
@AllArgsConstructor
public enum PayWalletBizType {

    PAYMENT(1),
    RECHARGE(2),
    REFUND(3);

    private final Integer type;
}
