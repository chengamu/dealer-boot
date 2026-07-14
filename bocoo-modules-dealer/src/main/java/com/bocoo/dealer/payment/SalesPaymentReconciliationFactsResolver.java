package com.bocoo.dealer.payment;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.pay.api.PaymentReconciliationFacts;
import com.bocoo.pay.api.PaymentReconciliationFactsResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesPaymentReconciliationFactsResolver implements PaymentReconciliationFactsResolver {
    private final SalesPaymentDocumentScopeResolver documentScopes;

    @Override
    public PaymentReconciliationFacts resolve(Long salesDocumentId) {
        SalesDocument document = documentScopes.findAccessibleDocument(salesDocumentId);
        if (document == null) {
            return PaymentReconciliationFacts.builder().exists(false)
                .salesDocumentId(salesDocumentId).build();
        }
        return PaymentReconciliationFacts.builder().exists(true)
            .salesDocumentId(document.getSalesDocumentId())
            .documentStatus(document.getDocumentStatus())
            .paymentStatus(document.getPaymentStatus())
            .payOrderId(document.getPayOrderId())
            .totalAmount(document.getTotalAmount())
            .currency(document.getCurrencyCode()).build();
    }
}
