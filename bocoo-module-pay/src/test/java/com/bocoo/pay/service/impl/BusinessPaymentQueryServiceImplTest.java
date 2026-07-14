package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.api.PaymentDocumentScopeResolver;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessPaymentQueryServiceImplTest extends PayServiceTestSupport {
    @Mock PaymentOrderQuerySupport queries;
    @Mock PaymentDocumentScopeSupport scopes;
    @Mock PaymentDocumentScopeResolver resolver;
    @Mock PayOrderDetailService details;
    @Mock PayOrderMapper orderMapper;
    @Mock PayOperatorContext operator;

    @Test
    void detailRejectsPaymentOwnedByAnotherTenant() {
        PayOrder order = new PayOrder();
        order.setId(5L);
        order.setSalesDocumentId(8L);
        order.setPayerTenantId(200L);
        when(orderMapper.selectById(5L)).thenReturn(order);
        when(scopes.required()).thenReturn(resolver);
        when(operator.tenantId()).thenReturn(100L);
        BusinessPaymentQueryServiceImpl service = new BusinessPaymentQueryServiceImpl(
            queries, scopes, details, orderMapper, operator);

        assertThatThrownBy(() -> service.detail(5L))
            .isInstanceOf(ServiceException.class).hasMessageContaining("current tenant");
        verify(resolver).requireAccessible(8L);
        verify(details, never()).getDetail(5L);
    }
}
