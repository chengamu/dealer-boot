package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment refund status.
 */
@Getter
@AllArgsConstructor
public enum PayRefundStatus {

    CREATE(0),
    SUCCESS(10),
    FAILURE(20);

    private final Integer status;
}
