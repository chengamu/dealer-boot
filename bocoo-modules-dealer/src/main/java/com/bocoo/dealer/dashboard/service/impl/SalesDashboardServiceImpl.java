package com.bocoo.dealer.dashboard.service.impl;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.repository.SalesDashboardQueryRepository;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScopeResolver;
import com.bocoo.dealer.dashboard.service.SalesDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesDashboardServiceImpl implements SalesDashboardService {
    private static final int TODO_LIMIT = 10;

    private final SalesDashboardScopeResolver scopeResolver;
    private final SalesDashboardQueryRepository repository;

    @Override
    public SalesDashboardVo getDashboard() {
        LocalDateTime now = TimeUtils.utcNow();
        LocalDate today = now.toLocalDate();
        LocalDate fromDate = today.minusDays(29);
        LocalDateTime periodStart = fromDate.atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        SalesDashboardScope scope = scopeResolver.resolve();
        SalesDashboardVo.Capabilities capabilities = scopeResolver.capabilities();

        SalesDashboardVo.QuoteSummary quoteSummary = capabilities.quote()
            ? repository.quoteSummary(scope, today) : null;
        SalesDashboardVo.OrderSummary orderSummary = capabilities.order()
            ? repository.orderSummary(scope, periodStart) : null;
        SalesDashboardVo.PaymentSummary paymentSummary = capabilities.payment()
            ? repository.paymentSummary(scope, monthStart) : null;
        SalesDashboardVo.FulfillmentSummary fulfillmentSummary = capabilities.fulfillment()
            ? repository.fulfillmentSummary(scope, capabilities.production(), capabilities.shipment()) : null;

        List<SalesDashboardVo.RecentQuote> recentQuotes = capabilities.quote()
            ? repository.recentQuotes(scope, periodStart) : List.of();
        List<SalesDashboardVo.RecentOrder> recentOrders = capabilities.order()
            ? repository.recentOrders(scope, periodStart) : List.of();

        List<SalesDashboardVo.Todo> todos = new ArrayList<>();
        if (capabilities.quote()) todos.addAll(repository.quoteTodos(scope, today));
        if (capabilities.payment()) todos.addAll(repository.paymentTodos(scope));
        if (capabilities.fulfillment()) {
            todos.addAll(repository.fulfillmentTodos(scope, capabilities.production(), capabilities.shipment()));
        }

        return new SalesDashboardVo(scope.type(), scope.label(), now, fromDate, today, capabilities,
            quoteSummary, orderSummary, paymentSummary, fulfillmentSummary, recentQuotes, recentOrders,
            normalizeTodos(todos));
    }

    private List<SalesDashboardVo.Todo> normalizeTodos(List<SalesDashboardVo.Todo> todos) {
        Map<String, SalesDashboardVo.Todo> distinct = new LinkedHashMap<>();
        todos.forEach(todo -> distinct.putIfAbsent(todo.type() + ":" + todo.sourceId(), todo));
        return distinct.values().stream()
            .sorted(Comparator.comparing(SalesDashboardVo.Todo::occurredAt,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .limit(TODO_LIMIT)
            .toList();
    }
}
