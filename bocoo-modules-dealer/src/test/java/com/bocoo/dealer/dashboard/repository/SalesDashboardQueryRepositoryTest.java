package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesDashboardQueryRepositoryTest {
    @Mock private CustomerQuoteMapper quoteMapper;
    @Mock private SalesDocumentMapper orderMapper;

    @Test
    void selfScopeAppliesOwnerRestrictionToEveryOrderSummaryQuery() {
        when(orderMapper.selectCount(any())).thenReturn(0L);
        SalesDashboardScope scope = new SalesDashboardScope("SELF", "sales", null, 41L);

        new SalesDashboardQueryRepository(quoteMapper, orderMapper)
            .orderSummary(scope, LocalDateTime.of(2026, 6, 13, 0, 0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<SalesDocument>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(orderMapper, times(3)).selectCount(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(query -> {
            assertThat(query.getSqlSegment()).contains("owner_user_id =");
            assertThat(query.getParamNameValuePairs()).containsValue(41L);
        });
    }
}
