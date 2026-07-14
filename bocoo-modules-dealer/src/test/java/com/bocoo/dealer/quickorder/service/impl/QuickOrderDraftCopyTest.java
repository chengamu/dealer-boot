package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.scope.SalesOwnershipResolver;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;
import com.bocoo.merchant.service.CustomerProfileService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuickOrderDraftCopyTest {
    @Test
    void copyInheritsFrozenSalesOwnership() {
        QuickOrderMapper mapper = mock(QuickOrderMapper.class);
        QuickOrderItemMapper itemMapper = mock(QuickOrderItemMapper.class);
        QuickOrderAccess access = mock(QuickOrderAccess.class);
        CustomerProfileService customerService = mock(CustomerProfileService.class);
        QuickOrderHeaderFactory headerFactory = new QuickOrderHeaderFactory(
            customerService, mock(SalesOwnershipResolver.class));
        QuickOrderItemWriter itemWriter = mock(QuickOrderItemWriter.class);
        QuickOrder source = source();
        CustomerProfileVo customer = new CustomerProfileVo();
        customer.setCustomerId(2L); customer.setCustomerName("Customer"); customer.setStatus("ENABLED");
        when(access.load(1L)).thenReturn(source);
        when(access.items(1L, 1L)).thenReturn(List.of());
        when(customerService.queryById(2L)).thenReturn(customer);
        doAnswer(invocation -> {
            ((QuickOrder) invocation.getArgument(0)).setQuickOrderId(2L);
            return 1;
        }).when(mapper).insert(any());

        QuickOrderDraftServiceImpl service = new QuickOrderDraftServiceImpl(
            mapper, itemMapper, access, headerFactory, itemWriter);
        Long id = service.copy(1L);

        org.mockito.ArgumentCaptor<QuickOrder> saved = org.mockito.ArgumentCaptor.forClass(QuickOrder.class);
        verify(mapper).insert(saved.capture());
        QuickOrder target = saved.getValue();
        assertThat(id).isEqualTo(2L);
        assertThat(target.getTenantId()).isEqualTo(1L);
        assertThat(target.getBusinessOrigin()).isEqualTo("INTERNAL");
        assertThat(target.getSalesStoreId()).isEqualTo(31L);
        assertThat(target.getDeptId()).isEqualTo(12L);
        assertThat(target.getOwnerUserId()).isEqualTo(8L);
    }

    private QuickOrder source() {
        QuickOrder row = new QuickOrder();
        row.setQuickOrderId(1L); row.setTenantId(1L); row.setBusinessOrigin("INTERNAL");
        row.setSalesStoreId(31L); row.setDeptId(12L); row.setOwnerUserId(8L);
        row.setCustomerId(2L); row.setCurrencyCode("USD");
        return row;
    }
}
