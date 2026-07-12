package com.bocoo.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayBankStatus {
    DRAFT(0),
    PENDING_REVIEW(5),
    SUCCESS(10),
    REJECTED(15),
    CLOSED(20);

    private final Integer status;
}
