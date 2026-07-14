package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesDashboardOrderAttentionRepository {
    private static final int ITEM_LIMIT = 3;

    private final SalesDashboardOrderMapper mapper;
    private final SalesDashboardQueries queries;

    public List<SalesDashboardVo.AttentionGroup> groups(SalesDashboardScope scope) {
        return TenantContextHolder.callWithIgnore(() -> List.of(
            group(scope, "PAYMENT_PENDING", "ORDER", queries.order(scope).eq("document_status", "SUBMITTED")
                .eq("payment_status", "UNPAID"),
                "SUBMITTED", "UNPAID", List.of(), List.of()),
            group(scope, "PRODUCTION_PENDING", "PRODUCTION", queries.fulfillmentOrder(scope)
                .eq("production_status", "PENDING"),
                null, "PAID", List.of("PENDING"), List.of()),
            group(scope, "SHIPMENT_PENDING", "SHIPMENT", queries.fulfillmentOrder(scope)
                .eq("production_status", "COMPLETED").in("shipment_status", "UNSHIPPED", "PARTIALLY_SHIPPED"),
                null, "PAID",
                List.of("COMPLETED"), List.of("UNSHIPPED", "PARTIALLY_SHIPPED"))
        ));
    }

    private SalesDashboardVo.AttentionGroup group(SalesDashboardScope scope, String reason, String module,
        QueryWrapper<SalesDocument> query, String status, String paymentStatus, List<String> production,
        List<String> shipment) {
        long count = mapper.selectCount(query);
        if ("PRODUCTION_PENDING".equals(reason)) query.orderByAsc("paid_time", "sales_document_id");
        else if ("SHIPMENT_PENDING".equals(reason)) query.orderByAsc("production_complete_time", "sales_document_id");
        else query.orderByAsc("submitted_time", "sales_document_id");
        List<SalesDashboardVo.AttentionItem> items = mapper.selectPage(new Page<>(1, ITEM_LIMIT, false), query)
            .getRecords().stream().map(row -> item(row, reason)).toList();
        SalesDashboardVo.TargetFilters filters = new SalesDashboardVo.TargetFilters(null, status, paymentStatus,
            production, shipment, null, null, null, scope.tenantId(), scope.businessOrigin(), scope.salesStoreId());
        return new SalesDashboardVo.AttentionGroup(reason, count,
            new SalesDashboardVo.DashboardTarget(module, filters), items);
    }

    private SalesDashboardVo.AttentionItem item(SalesDocument row, String reason) {
        return new SalesDashboardVo.AttentionItem("ORDER", row.getSalesDocumentId(), row.getOrderNo(),
            row.getCustomerName(), row.getProjectName(), switch (reason) {
                case "PRODUCTION_PENDING" -> row.getPaidTime();
                case "SHIPMENT_PENDING" -> row.getProductionCompleteTime();
                default -> row.getSubmittedTime();
            });
    }
}
