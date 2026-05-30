package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment transfer status.
 */
@Getter
@AllArgsConstructor
public enum PayTransferStatus {

    WAITING(0),
    SUCCESS(10),
    FAILURE(20);

    private final Integer status;
}
