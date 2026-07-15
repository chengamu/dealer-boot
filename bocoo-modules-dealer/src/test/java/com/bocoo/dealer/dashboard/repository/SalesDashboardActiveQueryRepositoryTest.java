package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesDashboardActiveQueryRepositoryTest {
    @Mock private SalesDashboardOrderMapper orderMapper;
    @Mock private SalesDashboardQuoteMapper quoteMapper;

    @Test
    void activeOrderAttentionQueriesDoNotApplyMonthCutoff() {
        when(orderMapper.selectCount(any())).thenReturn(1L);
        when(orderMapper.selectPage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        SalesDashboardScope scope = scope();

        new SalesDashboardOrderAttentionRepository(orderMapper, new SalesDashboardQueries()).groups(scope);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<SalesDocument>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(orderMapper, times(3)).selectCount(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(query -> {
            assertThat(query.getSqlSegment()).doesNotContain(">=", " BETWEEN ");
            assertThat(query.getParamNameValuePairs()).containsValues(1L, "INTERNAL", 31L);
        });
    }

    @Test
    void recentOrdersHaveStableLimitAndNoRollingDatePredicate() {
        when(orderMapper.selectPage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        new SalesDashboardRecentRepository(quoteMapper, orderMapper, new SalesDashboardQueries()).orders(scope());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Page<SalesDocument>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<SalesDocument>> queryCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(orderMapper).selectPage(pageCaptor.capture(), queryCaptor.capture());
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(5);
        assertThat(queryCaptor.getValue().getSqlSegment())
            .doesNotContain("submitted_time >=")
            .contains("ORDER BY submitted_time DESC,sales_document_id DESC");
    }

    @Test
    void quoteAttentionGroupsUseNonOverlappingValidityWindows() {
        when(quoteMapper.selectCount(any())).thenReturn(1L);
        when(quoteMapper.selectPage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        LocalDate today = LocalDate.of(2026, 7, 14);

        new SalesDashboardQuoteAttentionRepository(quoteMapper, new SalesDashboardQueries()).groups(scope(), today);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<CustomerQuote>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(quoteMapper, times(2)).selectCount(captor.capture());
        assertThat(captor.getAllValues().get(0).getSqlSegment()).contains("valid_until BETWEEN");
        assertThat(captor.getAllValues().get(0).getParamNameValuePairs())
            .containsValues(today, today.plusDays(7));
        assertThat(captor.getAllValues().get(1).getSqlSegment()).contains("valid_until >");
        assertThat(captor.getAllValues().get(1).getParamNameValuePairs())
            .containsValue(today.plusDays(7));
    }

    private SalesDashboardScope scope() {
        return new SalesDashboardScope("INTERNAL", "sales", 1L, "INTERNAL", 31L, "Internal Sales");
    }
}
