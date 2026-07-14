package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private SalesDashboardScope scope() {
        return new SalesDashboardScope("INTERNAL", "sales", 1L, "INTERNAL", 31L, "Internal Sales");
    }
}
