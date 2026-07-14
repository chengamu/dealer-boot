package com.bocoo.dealer.dashboard.service;

import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.repository.SalesDashboardQueryRepository;
import com.bocoo.dealer.dashboard.scope.SalesDashboardCapabilitiesResolver;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScopeResolver;
import com.bocoo.dealer.dashboard.service.impl.SalesDashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesDashboardServiceImplTest {
    @Mock private SalesDashboardScopeResolver scopeResolver;
    @Mock private SalesDashboardCapabilitiesResolver capabilitiesResolver;
    @Mock private SalesDashboardQueryRepository repository;
    @Mock private SalesDashboardPeriodFactory periodFactory;

    @Test
    void businessDashboardReusesOneScopeAndPeriodForEverySection() {
        SalesDashboardScope scope = new SalesDashboardScope(
            "MERCHANT", "merchant", 300001L, "MERCHANT", null, null);
        SalesDashboardPeriod period = period();
        SalesDashboardVo.Capabilities capabilities = capabilities();
        SalesDashboardVo.Summary summary = summary();
        when(scopeResolver.resolveBusiness()).thenReturn(scope);
        when(capabilitiesResolver.resolve(false)).thenReturn(capabilities);
        when(periodFactory.current()).thenReturn(period);
        when(repository.summary(scope, period)).thenReturn(summary);
        when(repository.recentQuotes(scope)).thenReturn(List.of());
        when(repository.recentOrders(scope)).thenReturn(List.of());
        when(repository.attentionGroups(scope, period)).thenReturn(List.of());

        SalesDashboardVo result = new SalesDashboardServiceImpl(
            scopeResolver, capabilitiesResolver, repository, periodFactory).getBusinessDashboard();

        assertThat(result.viewType()).isEqualTo("MERCHANT");
        assertThat(result.periodStart()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(result.summary()).isSameAs(summary);
        verify(repository).summary(scope, period);
        verify(repository).recentQuotes(scope);
        verify(repository).recentOrders(scope);
        verify(repository).attentionGroups(scope, period);
    }

    @Test
    void platformDashboardKeepsStableGroupPriorityAndLimit() {
        SalesDashboardScope scope = new SalesDashboardScope("PLATFORM", "ops", null, null, null, null);
        SalesDashboardPeriod period = period();
        when(scopeResolver.resolvePlatform()).thenReturn(scope);
        when(capabilitiesResolver.resolve(true)).thenReturn(capabilities());
        when(periodFactory.current()).thenReturn(period);
        when(repository.summary(scope, period)).thenReturn(summary());
        when(repository.recentQuotes(scope)).thenReturn(List.of());
        when(repository.recentOrders(scope)).thenReturn(List.of());
        when(repository.attentionGroups(scope, period)).thenReturn(List.of(
            group("QUOTE_UNCONVERTED", 4), group("SHIPMENT_PENDING", 3), group("PAYMENT_PENDING", 9),
            group("QUOTE_EXPIRING", 2), group("PRODUCTION_PENDING", 5), group("IGNORED", 8)));

        SalesDashboardVo result = new SalesDashboardServiceImpl(
            scopeResolver, capabilitiesResolver, repository, periodFactory).getPlatformDashboard();

        assertThat(result.attentionGroups()).extracting(SalesDashboardVo.AttentionGroup::reasonCode)
            .containsExactly("PAYMENT_PENDING", "PRODUCTION_PENDING", "SHIPMENT_PENDING", "QUOTE_EXPIRING");
    }

    private SalesDashboardPeriod period() {
        return new SalesDashboardPeriod(LocalDateTime.of(2026, 7, 13, 4, 0), LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 13), LocalDateTime.of(2026, 6, 30, 16, 0),
            LocalDateTime.of(2026, 7, 31, 16, 0));
    }

    private SalesDashboardVo.Capabilities capabilities() {
        return new SalesDashboardVo.Capabilities(true, true, true, true, true, false, false, false);
    }

    private SalesDashboardVo.Summary summary() {
        return new SalesDashboardVo.Summary(1, 1, 1, 1, 0, 1, BigDecimal.ONE, 1, BigDecimal.ONE, "USD",
            null, null, null, null);
    }

    private SalesDashboardVo.AttentionGroup group(String reason, long count) {
        return new SalesDashboardVo.AttentionGroup(reason, count, null, List.of());
    }
}
