package com.bocoo.dealer.dashboard.repository;

import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.service.SalesDashboardPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesDashboardQueryRepository {
    private final SalesDashboardSummaryRepository summaryRepository;
    private final SalesDashboardRecentRepository recentRepository;
    private final SalesDashboardQuoteAttentionRepository quoteAttentionRepository;
    private final SalesDashboardOrderAttentionRepository orderAttentionRepository;

    public SalesDashboardVo.Summary summary(SalesDashboardScope scope, SalesDashboardPeriod period) {
        return summaryRepository.summary(scope, period);
    }

    public List<SalesDashboardVo.RecentQuote> recentQuotes(SalesDashboardScope scope) {
        return recentRepository.quotes(scope);
    }

    public List<SalesDashboardVo.RecentOrder> recentOrders(SalesDashboardScope scope) {
        return recentRepository.orders(scope);
    }

    public List<SalesDashboardVo.AttentionGroup> attentionGroups(SalesDashboardScope scope, SalesDashboardPeriod period) {
        List<SalesDashboardVo.AttentionGroup> groups = new ArrayList<>();
        groups.addAll(quoteAttentionRepository.groups(scope, period.periodEnd()));
        groups.addAll(orderAttentionRepository.groups(scope));
        return groups;
    }
}
