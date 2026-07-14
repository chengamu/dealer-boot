package com.bocoo.pay.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Implemented by the sales module, which owns document data scope. */
public interface PaymentDocumentScopeResolver {
    List<Long> accessibleDocumentIds(PaymentDocumentFilter filter);

    PaymentDocumentFacts requireAccessible(Long salesDocumentId);

    Map<Long, PaymentDocumentFacts> resolveFacts(Collection<Long> salesDocumentIds);
}
