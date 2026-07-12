package com.bocoo.dealer.dashboard.service;

import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.repository.SalesDashboardQueryRepository;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScopeResolver;
import com.bocoo.dealer.dashboard.service.impl.SalesDashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesDashboardServiceImplTest {
    @Mock private SalesDashboardScopeResolver scopeResolver;
    @Mock private SalesDashboardQueryRepository repository;

    @Test
    void factoryRoleOnlyLoadsFulfillmentSection() {
        SalesDashboardScope scope = new SalesDashboardScope("FULFILLMENT", "factory_production", null, null);
        SalesDashboardVo.Capabilities capabilities = new SalesDashboardVo.Capabilities(
            false, false, false, false, false, true, true, false, false, false, false);
        SalesDashboardVo.FulfillmentSummary summary = new SalesDashboardVo.FulfillmentSummary(3, 2, 0, 0, 5);
        when(scopeResolver.resolve()).thenReturn(scope);
        when(scopeResolver.capabilities()).thenReturn(capabilities);
        when(repository.fulfillmentSummary(scope, true, false)).thenReturn(summary);
        when(repository.fulfillmentTodos(scope, true, false)).thenReturn(List.of());

        SalesDashboardVo result = new SalesDashboardServiceImpl(scopeResolver, repository).getDashboard();

        assertThat(result.scopeType()).isEqualTo("FULFILLMENT");
        assertThat(result.fulfillmentSummary()).isEqualTo(summary);
        assertThat(result.quoteSummary()).isNull();
        assertThat(result.orderSummary()).isNull();
        assertThat(result.paymentSummary()).isNull();
        assertThat(result.recentQuotes()).isEmpty();
        assertThat(result.recentOrders()).isEmpty();
        verify(repository, never()).quoteSummary(any(), any());
        verify(repository, never()).orderSummary(any(), any());
        verify(repository, never()).paymentSummary(any(), any());
        verify(repository).fulfillmentSummary(eq(scope), eq(true), eq(false));
    }
}
