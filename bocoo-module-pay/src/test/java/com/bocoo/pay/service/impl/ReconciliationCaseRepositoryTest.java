package com.bocoo.pay.service.impl;

import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.enums.ReconciliationAnomalyType;
import com.bocoo.pay.enums.ReconciliationSeverity;
import com.bocoo.pay.mapper.PayReconciliationCaseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationCaseRepositoryTest extends PayServiceTestSupport {
    @Mock PayReconciliationCaseMapper mapper;

    @Test
    void repeatedFindingUpdatesExistingOpenCaseInsteadOfCreatingAnother() {
        PayReconciliationCase existing = new PayReconciliationCase();
        existing.setCaseId(9L);
        existing.setStatus("OPEN");
        existing.setVersion(2);
        when(mapper.selectOne(any(), eq(false))).thenReturn(existing);
        when(mapper.update(any(), any())).thenReturn(1);
        ReconciliationCaseRepository repository = new ReconciliationCaseRepository(mapper);
        PayOrder order = new PayOrder();
        order.setId(3L);
        order.setSalesDocumentId(4L);
        ReconciliationFinding finding = new ReconciliationFinding(
            ReconciliationAnomalyType.PAYMENT_SUCCESS_ORDER_UNPAID, ReconciliationSeverity.WARNING,
            "order_payment_not_written", "Order is unpaid", "{}", "{}");

        PayReconciliationCase result = repository.upsert(order,
            PaymentDocumentFacts.builder().salesDocumentId(4L).businessOrigin("MERCHANT").tenantId(20L).build(), finding);

        assertThat(result.getCaseId()).isEqualTo(9L);
        assertThat(result.getVersion()).isEqualTo(3);
        verify(mapper, never()).insert(any());
    }
}
