package com.bocoo.dealer.payment;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.pay.api.PaymentReconciliationFacts;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesPaymentReconciliationFactsResolverTest {
    private final SalesPaymentDocumentScopeResolver documentScopes = mock(SalesPaymentDocumentScopeResolver.class);
    private final SalesPaymentReconciliationFactsResolver resolver =
        new SalesPaymentReconciliationFactsResolver(documentScopes);

    @Test
    void returnsFrozenSalesDocumentFactsWithoutRecalculation() {
        SalesDocument document = new SalesDocument();
        document.setSalesDocumentId(10L);
        document.setDocumentStatus("SUBMITTED");
        document.setPaymentStatus("UNPAID");
        document.setPayOrderId(20L);
        document.setTotalAmount(new BigDecimal("878.40"));
        document.setCurrencyCode("USD");
        when(documentScopes.findAccessibleDocument(10L)).thenReturn(document);

        PaymentReconciliationFacts facts = resolver.resolve(10L);

        assertThat(facts.isExists()).isTrue();
        assertThat(facts.getSalesDocumentId()).isEqualTo(10L);
        assertThat(facts.getDocumentStatus()).isEqualTo("SUBMITTED");
        assertThat(facts.getPaymentStatus()).isEqualTo("UNPAID");
        assertThat(facts.getPayOrderId()).isEqualTo(20L);
        assertThat(facts.getTotalAmount()).isEqualByComparingTo("878.40");
        assertThat(facts.getCurrency()).isEqualTo("USD");
        verify(documentScopes).findAccessibleDocument(10L);
    }

    @Test
    void missingSalesDocumentReturnsMissingFact() {
        when(documentScopes.findAccessibleDocument(99L)).thenReturn(null);

        PaymentReconciliationFacts facts = resolver.resolve(99L);

        assertThat(facts.isExists()).isFalse();
        assertThat(facts.getSalesDocumentId()).isEqualTo(99L);
    }
}
