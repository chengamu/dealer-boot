package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SalesDashboardQuoteAttentionRepository {
    private static final int ITEM_LIMIT = 3;

    private final SalesDashboardQuoteMapper mapper;
    private final SalesDashboardQueries queries;

    public List<SalesDashboardVo.AttentionGroup> groups(SalesDashboardScope scope, LocalDate today) {
        return TenantContextHolder.callWithIgnore(() -> List.of(
            group(scope, "QUOTE_EXPIRING", queries.quote(scope).eq("status", "CONFIRMED")
                .isNull("sales_document_id").between("valid_until", today, today.plusDays(7)),
                today, today.plusDays(7)),
            group(scope, "QUOTE_UNCONVERTED", queries.quote(scope).eq("status", "CONFIRMED")
                .isNull("sales_document_id").gt("valid_until", today.plusDays(7)), today.plusDays(8), null)
        ));
    }

    private SalesDashboardVo.AttentionGroup group(SalesDashboardScope scope, String reason,
        QueryWrapper<CustomerQuote> query, LocalDate from, LocalDate to) {
        long count = mapper.selectCount(query);
        if ("QUOTE_EXPIRING".equals(reason)) query.orderByAsc("valid_until", "quote_id");
        else query.orderByAsc("update_time", "quote_id");
        List<SalesDashboardVo.AttentionItem> items = mapper.selectPage(new Page<>(1, ITEM_LIMIT, false), query)
            .getRecords().stream().map(this::item).toList();
        SalesDashboardVo.TargetFilters filters = new SalesDashboardVo.TargetFilters(null, "CONFIRMED", null,
            List.of(), List.of(), true, from, to, scope.tenantId(), scope.businessOrigin(), scope.salesStoreId());
        return new SalesDashboardVo.AttentionGroup(reason, count,
            new SalesDashboardVo.DashboardTarget("QUOTE", filters), items);
    }

    private SalesDashboardVo.AttentionItem item(CustomerQuote row) {
        return new SalesDashboardVo.AttentionItem("QUOTE", row.getQuoteId(), row.getQuoteNo(), row.getCustomerName(),
            row.getProjectName(), row.getValidUntil() == null ? row.getUpdateTime() : row.getValidUntil().atStartOfDay());
    }
}
