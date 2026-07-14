package com.bocoo.pay.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentDocumentFacts {
    Long salesDocumentId;
    String businessOrigin;
    Long tenantId;
    Long salesStoreId;
    Long deptId;
    Long ownerUserId;
    String subjectName;
}
