package com.bocoo.pay.service.impl;

import com.bocoo.pay.api.PaymentDocumentScopeResolver;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankTransferQuerySupportTest extends PayServiceTestSupport {
    @Mock private PayOrderExtensionMapper extensionMapper;
    @Mock private PayOrderMapper orderMapper;
    @Mock private PaymentDocumentScopeSupport scopes;
    @Mock private PaymentDocumentScopeResolver documentScopes;

    @Test
    void pageByIdsLoadsOnlyCurrentPageAndPreservesDatabaseOrder() {
        PayOrderExtension first = extension(1L, 101L);
        PayOrderExtension second = extension(2L, 102L);
        when(extensionMapper.selectBatchIds(List.of(2L, 1L))).thenReturn(List.of(first, second));
        when(orderMapper.selectBatchIds(List.of(102L, 101L))).thenReturn(List.of(order(101L), order(102L)));
        when(scopes.required()).thenReturn(documentScopes);
        when(documentScopes.resolveFacts(List.of(1002L, 1001L))).thenReturn(Map.of());
        BankTransferQuerySupport support = new BankTransferQuerySupport(extensionMapper, orderMapper, scopes);

        var result = support.pageByIds(new PaymentScopePage(List.of(2L, 1L), 25));

        assertThat(result.getTotal()).isEqualTo(25);
        assertThat(result.getRows()).extracting("extensionId").containsExactly(2L, 1L);
    }

    private PayOrderExtension extension(Long id, Long orderId) {
        PayOrderExtension row = new PayOrderExtension();
        row.setId(id);
        row.setOrderId(orderId);
        return row;
    }

    private PayOrder order(Long id) {
        PayOrder row = new PayOrder();
        row.setId(id);
        row.setSalesDocumentId(id + 900);
        return row;
    }
}
