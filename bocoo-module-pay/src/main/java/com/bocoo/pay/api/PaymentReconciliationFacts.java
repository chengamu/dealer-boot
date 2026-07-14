package com.bocoo.pay.api;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PaymentReconciliationFacts {
    boolean exists;
    Long salesDocumentId;
    String documentStatus;
    String paymentStatus;
    Long payOrderId;
    BigDecimal totalAmount;
    String currency;
}
