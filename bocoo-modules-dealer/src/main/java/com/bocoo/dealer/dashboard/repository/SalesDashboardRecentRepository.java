package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesDashboardRecentRepository {
    private static final int RECENT_LIMIT = 5;

    private final SalesDashboardQuoteMapper quoteMapper;
    private final SalesDashboardOrderMapper orderMapper;
    private final SalesDashboardQueries queries;

    public List<SalesDashboardVo.RecentQuote> quotes(SalesDashboardScope scope) {
        return TenantContextHolder.callWithIgnore(() -> quoteMapper.selectPage(new Page<>(1, RECENT_LIMIT, false),
                queries.quote(scope).orderByDesc("update_time", "create_time", "quote_id"))
            .getRecords().stream().map(row -> quote(scope, row)).toList());
    }

    public List<SalesDashboardVo.RecentOrder> orders(SalesDashboardScope scope) {
        return TenantContextHolder.callWithIgnore(() -> orderMapper.selectPage(new Page<>(1, RECENT_LIMIT, false),
                queries.order(scope).orderByDesc("submitted_time", "sales_document_id"))
            .getRecords().stream().map(row -> order(scope, row)).toList());
    }

    private SalesDashboardVo.RecentQuote quote(SalesDashboardScope scope, CustomerQuote row) {
        return new SalesDashboardVo.RecentQuote(row.getQuoteId(), row.getQuoteNo(), row.getCustomerName(),
            row.getProjectName(), row.getStatus(), row.getValidUntil(), row.getTotalAmount(), row.getCurrencyCode(),
            row.getSalesDocumentId(), row.getUpdateTime() == null ? row.getCreateTime() : row.getUpdateTime(),
            row.getBusinessOrigin(), row.getTenantId(), row.getSalesStoreId(), detailTarget(scope, "QUOTE", row.getQuoteId()));
    }

    private SalesDashboardVo.RecentOrder order(SalesDashboardScope scope, SalesDocument row) {
        return new SalesDashboardVo.RecentOrder(row.getSalesDocumentId(), row.getOrderNo(), row.getSourceType(),
            row.getCustomerName(), row.getProjectName(), row.getDocumentStatus(), row.getPaymentStatus(),
            row.getProductionStatus(), row.getShipmentStatus(), row.getTotalAmount(), row.getCurrencyCode(),
            row.getSubmittedTime(), row.getBusinessOrigin(), row.getTenantId(), row.getSalesStoreId(),
            detailTarget(scope, "ORDER", row.getSalesDocumentId()));
    }

    private SalesDashboardVo.DashboardTarget detailTarget(SalesDashboardScope scope, String module, Long id) {
        SalesDashboardVo.TargetFilters base = SalesDashboardVo.TargetFilters.scope(scope.tenantId(),
            scope.businessOrigin(), scope.salesStoreId());
        return new SalesDashboardVo.DashboardTarget(module, new SalesDashboardVo.TargetFilters(id, null, null,
            List.of(), List.of(), null, null, null, base.tenantId(), base.businessOrigin(), base.salesStoreId()));
    }
}
