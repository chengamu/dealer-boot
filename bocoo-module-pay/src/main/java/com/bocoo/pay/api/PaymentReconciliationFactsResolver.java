package com.bocoo.pay.api;

/** Implemented by the sales module; returns only reconciliation-safe facts. */
public interface PaymentReconciliationFactsResolver {
    PaymentReconciliationFacts resolve(Long salesDocumentId);
}
