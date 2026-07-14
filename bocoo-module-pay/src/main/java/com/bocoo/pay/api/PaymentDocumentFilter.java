package com.bocoo.pay.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentDocumentFilter {
    String businessOrigin;
    Long subjectId;
    String keyword;
}
