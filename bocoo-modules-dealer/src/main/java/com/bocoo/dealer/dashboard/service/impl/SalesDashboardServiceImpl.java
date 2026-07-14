package com.bocoo.dealer.dashboard.service.impl;

import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.repository.SalesDashboardQueryRepository;
import com.bocoo.dealer.dashboard.scope.SalesDashboardCapabilitiesResolver;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScopeResolver;
import com.bocoo.dealer.dashboard.service.SalesDashboardPeriod;
import com.bocoo.dealer.dashboard.service.SalesDashboardPeriodFactory;
import com.bocoo.dealer.dashboard.service.SalesDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesDashboardServiceImpl implements SalesDashboardService {
    private static final int GROUP_LIMIT = 4;
    private static final List<String> BUSINESS_PRIORITY = List.of(
        "QUOTE_EXPIRING", "PAYMENT_PENDING", "SHIPMENT_PENDING", "QUOTE_UNCONVERTED", "PRODUCTION_PENDING");
    private static final List<String> PLATFORM_PRIORITY = List.of(
        "PAYMENT_PENDING", "PRODUCTION_PENDING", "SHIPMENT_PENDING", "QUOTE_EXPIRING", "QUOTE_UNCONVERTED");

    private final SalesDashboardScopeResolver scopeResolver;
    private final SalesDashboardCapabilitiesResolver capabilitiesResolver;
    private final SalesDashboardQueryRepository repository;
    private final SalesDashboardPeriodFactory periodFactory;

    @Override
    public SalesDashboardVo getBusinessDashboard() {
        return build(scopeResolver.resolveBusiness(), false);
    }

    @Override
    public SalesDashboardVo getPlatformDashboard() {
        return build(scopeResolver.resolvePlatform(), true);
    }

    private SalesDashboardVo build(SalesDashboardScope scope, boolean platformView) {
        SalesDashboardPeriod period = periodFactory.current();
        SalesDashboardVo.Capabilities capabilities = capabilitiesResolver.resolve(platformView);
        return new SalesDashboardVo(scope.viewType(), scope.label(), scope.salesStoreId(), scope.salesStoreName(),
            period.periodStart(), period.periodEnd(), period.dataAsOf(), capabilities,
            repository.summary(scope, period), repository.recentQuotes(scope), repository.recentOrders(scope),
            visibleGroups(repository.attentionGroups(scope, period), platformView));
    }

    private List<SalesDashboardVo.AttentionGroup> visibleGroups(
        List<SalesDashboardVo.AttentionGroup> groups, boolean platformView
    ) {
        List<String> priority = platformView ? PLATFORM_PRIORITY : BUSINESS_PRIORITY;
        return groups.stream().filter(group -> group.totalCount() > 0)
            .sorted(Comparator.comparingInt(group -> priorityIndex(priority, group.reasonCode())))
            .limit(GROUP_LIMIT).toList();
    }

    private int priorityIndex(List<String> priority, String reasonCode) {
        int index = priority.indexOf(reasonCode);
        return index < 0 ? Integer.MAX_VALUE : index;
    }
}
