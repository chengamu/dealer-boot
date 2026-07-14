package com.bocoo.dealer.dashboard.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SalesDashboardVo(
    String viewType,
    String scopeLabel,
    Long salesStoreId,
    String salesStoreName,
    LocalDate periodStart,
    LocalDate periodEnd,
    LocalDateTime dataAsOf,
    Capabilities capabilities,
    Summary summary,
    List<RecentQuote> recentQuotes,
    List<RecentOrder> recentOrders,
    List<AttentionGroup> attentionGroups
) {
    public record Capabilities(
        boolean quote,
        boolean order,
        boolean payment,
        boolean production,
        boolean shipment,
        boolean createQuote,
        boolean createCustomer,
        boolean quickOrder
    ) {
    }

    public record Summary(
        long activeQuoteCount,
        long expiringQuoteCount,
        long activeProductionCount,
        long pendingProductionCount,
        long inProductionCount,
        long paidOrderCount,
        BigDecimal paidAmount,
        long pendingPaymentOrderCount,
        BigDecimal pendingPaymentAmount,
        String currencyCode,
        DashboardTarget activeQuoteTarget,
        DashboardTarget productionTarget,
        DashboardTarget paidTarget,
        DashboardTarget pendingPaymentTarget
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
        LocalDateTime updatedAt,
        String businessOrigin,
        Long tenantId,
        Long salesStoreId,
        DashboardTarget target
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
        LocalDateTime submittedAt,
        String businessOrigin,
        Long tenantId,
        Long salesStoreId,
        DashboardTarget target
    ) {
    }

    public record AttentionGroup(
        String reasonCode,
        long totalCount,
        DashboardTarget target,
        List<AttentionItem> items
    ) {
    }

    public record AttentionItem(
        String type,
        Long sourceId,
        String sourceNo,
        String customerName,
        String projectName,
        LocalDateTime occurredAt
    ) {
    }

    public record DashboardTarget(String module, TargetFilters filters) {
    }

    public record TargetFilters(
        Long id,
        String status,
        String paymentStatus,
        List<String> productionStatuses,
        List<String> shipmentStatuses,
        Boolean unconverted,
        LocalDate dateFrom,
        LocalDate dateTo,
        Long tenantId,
        String businessOrigin,
        Long salesStoreId
    ) {
        public static TargetFilters scope(Long tenantId, String businessOrigin, Long salesStoreId) {
            return new TargetFilters(null, null, null, List.of(), List.of(), null, null, null,
                tenantId, businessOrigin, salesStoreId);
        }
    }
}
