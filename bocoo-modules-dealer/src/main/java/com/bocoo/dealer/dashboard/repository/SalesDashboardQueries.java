package com.bocoo.dealer.dashboard.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import org.springframework.stereotype.Component;

@Component
public class SalesDashboardQueries {
    public QueryWrapper<CustomerQuote> quote(SalesDashboardScope scope) {
        QueryWrapper<CustomerQuote> query = new QueryWrapper<CustomerQuote>().eq("del_flag", "0");
        applyScope(query, scope);
        return query;
    }

    public QueryWrapper<SalesDocument> order(SalesDashboardScope scope) {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>().eq("del_flag", "0");
        applyScope(query, scope);
        return query;
    }

    public QueryWrapper<SalesDocument> fulfillmentOrder(SalesDashboardScope scope) {
        return order(scope).eq("document_status", "SUBMITTED").eq("payment_status", "PAID");
    }

    private void applyScope(QueryWrapper<?> query, SalesDashboardScope scope) {
        query.eq(scope.tenantId() != null, "tenant_id", scope.tenantId());
        query.eq(scope.businessOrigin() != null, "business_origin", scope.businessOrigin());
        query.eq(scope.salesStoreId() != null, "sales_store_id", scope.salesStoreId());
    }
}
