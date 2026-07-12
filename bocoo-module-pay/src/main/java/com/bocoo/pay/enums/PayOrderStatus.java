package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment order status.
 */
@Getter
@AllArgsConstructor
public enum PayOrderStatus {

    WAITING(0),
    PROCESSING(5),
    SUCCESS(10),
    CLOSED(20);

    private final Integer status;

    public static boolean isWaiting(Integer status) {
        return WAITING.status.equals(status);
    }

    public static boolean canConfirm(Integer status) {
        return WAITING.status.equals(status) || PROCESSING.status.equals(status);
    }
}
