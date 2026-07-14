package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.dashboard.service.SalesDashboardPeriod;
import com.bocoo.dealer.domain.entity.SalesDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesDashboardSummaryRepository {
    private static final String CURRENCY_USD = "USD";

    private final SalesDashboardQuoteMapper quoteMapper;
    private final SalesDashboardOrderMapper orderMapper;
    private final SalesDashboardQueries queries;

    public SalesDashboardVo.Summary summary(SalesDashboardScope scope, SalesDashboardPeriod period) {
        return TenantContextHolder.callWithIgnore(() -> summarize(scope, period));
    }

    private SalesDashboardVo.Summary summarize(SalesDashboardScope scope, SalesDashboardPeriod period) {
        LocalDate today = period.periodEnd();
        long activeQuotes = quoteMapper.selectCount(queries.quote(scope).eq("status", "CONFIRMED")
            .ge("valid_until", today));
        long expiringQuotes = quoteMapper.selectCount(queries.quote(scope).eq("status", "CONFIRMED")
            .isNull("sales_document_id").between("valid_until", today, today.plusDays(7)));
        long pendingProduction = orderMapper.selectCount(queries.fulfillmentOrder(scope)
            .eq("production_status", "PENDING"));
        long inProduction = orderMapper.selectCount(queries.fulfillmentOrder(scope)
            .eq("production_status", "IN_PRODUCTION"));
        QueryWrapper<SalesDocument> paid = queries.order(scope).eq("payment_status", "PAID")
            .eq("currency_code", CURRENCY_USD).ge("paid_time", period.monthStartUtc())
            .lt("paid_time", period.nextMonthStartUtc());
        QueryWrapper<SalesDocument> pending = queries.order(scope).eq("document_status", "SUBMITTED")
            .eq("payment_status", "UNPAID").eq("currency_code", CURRENCY_USD);
        return new SalesDashboardVo.Summary(activeQuotes, expiringQuotes,
            pendingProduction + inProduction, pendingProduction, inProduction,
            orderMapper.selectCount(paid), sum(paid), orderMapper.selectCount(pending), sum(pending), CURRENCY_USD,
            target(scope, "QUOTE", "CONFIRMED", null, List.of(), List.of(), null, today, null),
            target(scope, "PRODUCTION", null, "PAID", List.of("PENDING", "IN_PRODUCTION"), List.of(), null, null, null),
            target(scope, "PAYMENT", null, "PAID", List.of(), List.of(), null,
                period.periodStart(), period.periodEnd()),
            target(scope, "ORDER", "SUBMITTED", "UNPAID", List.of(), List.of(), null, null, null));
    }

    private BigDecimal sum(QueryWrapper<SalesDocument> query) {
        Object value = orderMapper.selectObjs(query.select("COALESCE(SUM(total_amount), 0)")).stream()
            .findFirst().orElse(BigDecimal.ZERO);
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }

    private SalesDashboardVo.DashboardTarget target(SalesDashboardScope scope, String module, String status,
        String paymentStatus, List<String> production, List<String> shipment, Boolean unconverted,
        LocalDate from, LocalDate to) {
        return new SalesDashboardVo.DashboardTarget(module, new SalesDashboardVo.TargetFilters(null, status,
            paymentStatus, production, shipment, unconverted, from, to, scope.tenantId(), scope.businessOrigin(),
            scope.salesStoreId()));
    }
}
