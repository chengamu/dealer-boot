package com.bocoo.dealer.dashboard.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SalesDashboardVo(
    String scopeType,
    String scopeLabel,
    LocalDateTime dataAsOf,
    LocalDate fromDate,
    LocalDate toDate,
    Capabilities capabilities,
    QuoteSummary quoteSummary,
    OrderSummary orderSummary,
    PaymentSummary paymentSummary,
    FulfillmentSummary fulfillmentSummary,
    List<RecentQuote> recentQuotes,
    List<RecentOrder> recentOrders,
    List<Todo> todos
) {
    public record Capabilities(
        boolean quote,
        boolean quoteDetail,
        boolean order,
        boolean orderDetail,
        boolean payment,
        boolean fulfillment,
        boolean production,
        boolean shipment,
        boolean createQuote,
        boolean createCustomer,
        boolean quickOrder
    ) {
    }

    public record QuoteSummary(long activeCount, long draftCount, long confirmedUnconvertedCount) {
    }

    public record OrderSummary(long activeCount, long periodSubmittedCount, long pendingPaymentCount) {
    }

    public record PaymentSummary(
        long pendingCount,
        BigDecimal pendingAmount,
        long paidThisMonthCount,
        BigDecimal paidThisMonthAmount,
        String currencyCode
    ) {
    }

    public record FulfillmentSummary(
        long pendingProductionCount,
        long inProductionCount,
        long pendingShipmentCount,
        long shippedCount,
        long completedCount
    ) {
    }

    public record RecentQuote(
        Long id,
        String quoteNo,
        String customerName,
        String projectName,
        String status,
        LocalDate validUntil,
        BigDecimal totalAmount,
        String currencyCode,
        Long salesDocumentId,
        LocalDateTime updatedAt
    ) {
    }

    public record RecentOrder(
        Long id,
        String orderNo,
        String sourceType,
        String customerName,
        String projectName,
        String documentStatus,
        String paymentStatus,
        String productionStatus,
        String shipmentStatus,
        BigDecimal totalAmount,
        String currencyCode,
        LocalDateTime submittedAt
    ) {
    }

    public record Todo(
        String type,
        Long sourceId,
        String sourceNo,
        String customerName,
        String projectName,
        String reasonCode,
        LocalDateTime occurredAt
    ) {
    }
}
