package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment notify business type.
 */
@Getter
@AllArgsConstructor
public enum PayNotifyType {

    ORDER(1),
    REFUND(2),
    TRANSFER(3);

    private final Integer type;
}
