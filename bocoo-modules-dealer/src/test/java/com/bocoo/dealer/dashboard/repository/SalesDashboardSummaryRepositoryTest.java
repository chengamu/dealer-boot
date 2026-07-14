package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.service.SalesDashboardPeriod;
import com.bocoo.dealer.domain.entity.SalesDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SalesDashboardSummaryRepositoryTest {
    @Mock private SalesDashboardQuoteMapper quoteMapper;
    @Mock private SalesDashboardOrderMapper orderMapper;

    @Test
    void summaryUsesBusinessMonthUtcBoundsAndStableTargets() {
        when(quoteMapper.selectCount(any())).thenReturn(7L, 2L);
        when(orderMapper.selectCount(any())).thenReturn(3L, 4L, 5L, 6L);
        when(orderMapper.selectObjs(any())).thenReturn(List.of(new BigDecimal("125.50")),
            List.of(new BigDecimal("80.00")));
        SalesDashboardScope scope = new SalesDashboardScope(
            "INTERNAL", "sales", 1L, "INTERNAL", 31L, "Internal Sales");
        SalesDashboardPeriod period = new SalesDashboardPeriod(LocalDateTime.of(2026, 7, 13, 4, 0),
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 13),
            LocalDateTime.of(2026, 6, 30, 16, 0), LocalDateTime.of(2026, 7, 31, 16, 0));

        var summary = new SalesDashboardSummaryRepository(quoteMapper, orderMapper, new SalesDashboardQueries())
            .summary(scope, period);

        assertThat(summary.activeQuoteCount()).isEqualTo(7);
        assertThat(summary.activeProductionCount()).isEqualTo(7);
        assertThat(summary.paidAmount()).isEqualByComparingTo("125.50");
        assertThat(summary.activeQuoteTarget().module()).isEqualTo("QUOTE");
        assertThat(summary.pendingPaymentTarget().filters().salesStoreId()).isEqualTo(31L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<SalesDocument>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(orderMapper, times(2)).selectObjs(captor.capture());
        QueryWrapper<SalesDocument> paidAmountQuery = captor.getAllValues().get(0);
        assertThat(paidAmountQuery.getSqlSegment()).contains("paid_time >=", "paid_time <", "currency_code");
        assertThat(paidAmountQuery.getParamNameValuePairs()).containsValues(
            LocalDateTime.of(2026, 6, 30, 16, 0), LocalDateTime.of(2026, 7, 31, 16, 0));
    }
}
