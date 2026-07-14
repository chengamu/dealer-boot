package com.bocoo.pay.api;

import java.util.List;

/** Current-page identifiers resolved inside the sales document data scope. */
public record PaymentScopePage(List<Long> ids, long total) {
    public PaymentScopePage {
        ids = ids == null ? List.of() : List.copyOf(ids);
    }
}
