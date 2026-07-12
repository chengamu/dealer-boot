package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesDashboardQueryRepository {
    private static final int RECENT_LIMIT = 5;
    private static final int TODO_LIMIT = 4;

    private final CustomerQuoteMapper quoteMapper;
    private final SalesDocumentMapper orderMapper;

    public SalesDashboardVo.QuoteSummary quoteSummary(SalesDashboardScope scope, LocalDate today) {
        return TenantContextHolder.callWithIgnore(() -> new SalesDashboardVo.QuoteSummary(
            quoteMapper.selectCount(quote(scope).ne("status", "VOID").ge("valid_until", today)),
            quoteMapper.selectCount(quote(scope).eq("status", "DRAFT")),
            quoteMapper.selectCount(quote(scope).eq("status", "CONFIRMED").isNull("sales_document_id"))
        ));
    }

    public SalesDashboardVo.OrderSummary orderSummary(SalesDashboardScope scope, LocalDateTime periodStart) {
        return TenantContextHolder.callWithIgnore(() -> new SalesDashboardVo.OrderSummary(
            orderMapper.selectCount(order(scope).eq("document_status", "SUBMITTED")),
            orderMapper.selectCount(order(scope).ge("submitted_time", periodStart)),
            orderMapper.selectCount(order(scope).eq("document_status", "SUBMITTED").eq("payment_status", "UNPAID"))
        ));
    }

    public SalesDashboardVo.PaymentSummary paymentSummary(SalesDashboardScope scope, LocalDateTime monthStart) {
        return TenantContextHolder.callWithIgnore(() -> {
            QueryWrapper<SalesDocument> pending = order(scope).eq("document_status", "SUBMITTED")
                .eq("payment_status", "UNPAID");
            QueryWrapper<SalesDocument> paid = order(scope).eq("payment_status", "PAID")
                .ge("paid_time", monthStart);
            return new SalesDashboardVo.PaymentSummary(
                orderMapper.selectCount(pending),
                sum(order(scope).eq("document_status", "SUBMITTED").eq("payment_status", "UNPAID")),
                orderMapper.selectCount(paid),
                sum(order(scope).eq("payment_status", "PAID").ge("paid_time", monthStart)),
                "USD"
            );
        });
    }

    public SalesDashboardVo.FulfillmentSummary fulfillmentSummary(
        SalesDashboardScope scope, boolean production, boolean shipment
    ) {
        return TenantContextHolder.callWithIgnore(() -> new SalesDashboardVo.FulfillmentSummary(
            production ? orderMapper.selectCount(fulfillmentOrder(scope).eq("production_status", "PENDING")) : 0,
            production ? orderMapper.selectCount(fulfillmentOrder(scope).eq("production_status", "IN_PRODUCTION")) : 0,
            shipment ? orderMapper.selectCount(fulfillmentOrder(scope).eq("production_status", "COMPLETED")
                .in("shipment_status", "UNSHIPPED", "PARTIALLY_SHIPPED")) : 0,
            shipment ? orderMapper.selectCount(fulfillmentOrder(scope).eq("shipment_status", "SHIPPED")) : 0,
            orderMapper.selectCount(order(scope).eq("document_status", "COMPLETED"))
        ));
    }

    public List<SalesDashboardVo.RecentQuote> recentQuotes(SalesDashboardScope scope, LocalDateTime periodStart) {
        return TenantContextHolder.callWithIgnore(() -> quoteMapper.selectPage(new Page<>(1, RECENT_LIMIT, false),
                quote(scope).ge("create_time", periodStart).orderByDesc("update_time", "create_time"))
            .getRecords().stream().map(this::recentQuote).toList());
    }

    public List<SalesDashboardVo.RecentOrder> recentOrders(SalesDashboardScope scope, LocalDateTime periodStart) {
        return TenantContextHolder.callWithIgnore(() -> orderMapper.selectPage(new Page<>(1, RECENT_LIMIT, false),
                order(scope).ge("submitted_time", periodStart).orderByDesc("submitted_time", "create_time"))
            .getRecords().stream().map(this::recentOrder).toList());
    }

    public List<SalesDashboardVo.Todo> quoteTodos(SalesDashboardScope scope, LocalDate today) {
        return TenantContextHolder.callWithIgnore(() -> {
            List<SalesDashboardVo.Todo> result = new ArrayList<>();
            selectQuotes(quote(scope).eq("status", "CONFIRMED").isNull("sales_document_id")
                .orderByAsc("valid_until", "update_time"), TODO_LIMIT)
                .forEach(row -> result.add(quoteTodo(row, "QUOTE_UNCONVERTED")));
            selectQuotes(quote(scope).eq("status", "CONFIRMED").between("valid_until", today, today.plusDays(7))
                .orderByAsc("valid_until"), TODO_LIMIT)
                .forEach(row -> result.add(quoteTodo(row, "QUOTE_EXPIRING")));
            return result;
        });
    }

    public List<SalesDashboardVo.Todo> paymentTodos(SalesDashboardScope scope) {
        return TenantContextHolder.callWithIgnore(() -> selectOrders(order(scope).eq("document_status", "SUBMITTED")
                .eq("payment_status", "UNPAID").orderByAsc("submitted_time"), TODO_LIMIT).stream()
            .map(row -> orderTodo(row, row.getPayOrderId() == null ? "PAYMENT_MISSING" : "PAYMENT_PENDING"))
            .toList());
    }

    public List<SalesDashboardVo.Todo> fulfillmentTodos(
        SalesDashboardScope scope, boolean production, boolean shipment
    ) {
        return TenantContextHolder.callWithIgnore(() -> {
            List<SalesDashboardVo.Todo> result = new ArrayList<>();
            if (production) {
                selectOrders(fulfillmentOrder(scope).eq("production_status", "PENDING")
                    .orderByAsc("paid_time"), TODO_LIMIT)
                    .forEach(row -> result.add(orderTodo(row, "PRODUCTION_PENDING")));
            }
            if (shipment) {
                selectOrders(fulfillmentOrder(scope).eq("production_status", "COMPLETED")
                    .in("shipment_status", "UNSHIPPED", "PARTIALLY_SHIPPED")
                    .orderByAsc("production_complete_time"), TODO_LIMIT)
                    .forEach(row -> result.add(orderTodo(row, "SHIPMENT_PENDING")));
            }
            return result;
        });
    }

    private QueryWrapper<CustomerQuote> quote(SalesDashboardScope scope) {
        QueryWrapper<CustomerQuote> query = new QueryWrapper<CustomerQuote>().eq("del_flag", "0");
        applyScope(query, scope);
        return query;
    }

    private QueryWrapper<SalesDocument> order(SalesDashboardScope scope) {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>().eq("del_flag", "0");
        applyScope(query, scope);
        return query;
    }

    private QueryWrapper<SalesDocument> fulfillmentOrder(SalesDashboardScope scope) {
        return order(scope).eq("document_status", "SUBMITTED").eq("payment_status", "PAID");
    }

    private void applyScope(QueryWrapper<?> query, SalesDashboardScope scope) {
        query.eq(scope.tenantId() != null, "tenant_id", scope.tenantId());
        query.eq(scope.ownerUserId() != null, "owner_user_id", scope.ownerUserId());
    }

    private BigDecimal sum(QueryWrapper<SalesDocument> query) {
        Object value = orderMapper.selectObjs(query.select("COALESCE(SUM(total_amount), 0)")).stream()
            .findFirst().orElse(BigDecimal.ZERO);
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }

    private List<CustomerQuote> selectQuotes(QueryWrapper<CustomerQuote> query, int limit) {
        return quoteMapper.selectPage(new Page<>(1, limit, false), query).getRecords();
    }

    private List<SalesDocument> selectOrders(QueryWrapper<SalesDocument> query, int limit) {
        return orderMapper.selectPage(new Page<>(1, limit, false), query).getRecords();
    }

    private SalesDashboardVo.RecentQuote recentQuote(CustomerQuote row) {
        return new SalesDashboardVo.RecentQuote(row.getQuoteId(), row.getQuoteNo(), row.getCustomerName(),
            row.getProjectName(), row.getStatus(), row.getValidUntil(), row.getTotalAmount(), row.getCurrencyCode(),
            row.getSalesDocumentId(), row.getUpdateTime() == null ? row.getCreateTime() : row.getUpdateTime());
    }

    private SalesDashboardVo.RecentOrder recentOrder(SalesDocument row) {
        return new SalesDashboardVo.RecentOrder(row.getSalesDocumentId(), row.getOrderNo(), row.getSourceType(),
            row.getCustomerName(), row.getProjectName(), row.getDocumentStatus(), row.getPaymentStatus(),
            row.getProductionStatus(), row.getShipmentStatus(), row.getTotalAmount(), row.getCurrencyCode(),
            row.getSubmittedTime());
    }

    private SalesDashboardVo.Todo quoteTodo(CustomerQuote row, String reason) {
        LocalDateTime occurredAt = row.getValidUntil() == null ? row.getUpdateTime() : row.getValidUntil().atStartOfDay();
        return new SalesDashboardVo.Todo("QUOTE", row.getQuoteId(), row.getQuoteNo(), row.getCustomerName(),
            row.getProjectName(), reason, occurredAt);
    }

    private SalesDashboardVo.Todo orderTodo(SalesDocument row, String reason) {
        LocalDateTime occurredAt = switch (reason) {
            case "PRODUCTION_PENDING" -> row.getPaidTime();
            case "SHIPMENT_PENDING" -> row.getProductionCompleteTime();
            default -> row.getSubmittedTime();
        };
        return new SalesDashboardVo.Todo("ORDER", row.getSalesDocumentId(), row.getOrderNo(), row.getCustomerName(),
            row.getProjectName(), reason, occurredAt);
    }
}
