package com.bocoo.pay.service.impl;

import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.mapper.PayOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOrderQuerySupportTest extends PayServiceTestSupport {
    @Mock
    private PayOrderMapper mapper;

    @Test
    void pageByIdsLoadsOnlyCurrentPageAndPreservesDatabaseOrder() {
        PayOrder first = order(1L, 101L);
        PayOrder second = order(2L, 102L);
        when(mapper.selectBatchIds(List.of(2L, 1L))).thenReturn(List.of(first, second));
        PaymentOrderQuerySupport support = new PaymentOrderQuerySupport(mapper);

        var result = support.pageByIds(new PaymentScopePage(List.of(2L, 1L), 25));

        assertThat(result.getTotal()).isEqualTo(25);
        assertThat(result.getRows()).extracting("payOrderId").containsExactly(2L, 1L);
    }

    private PayOrder order(Long id, Long documentId) {
        PayOrder order = new PayOrder();
        order.setId(id);
        order.setSalesDocumentId(documentId);
        return order;
    }
}
