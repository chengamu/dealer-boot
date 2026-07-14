package com.bocoo.dealer.dashboard.repository;

import com.bocoo.common.mybatis.annotation.DataPermission;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardOrderMapper;
import com.bocoo.dealer.dashboard.mapper.SalesDashboardQuoteMapper;
import com.bocoo.dealer.dashboard.scope.SalesDashboardScope;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SalesDashboardQueryRepositoryTest {
    private final SalesDashboardQueries queries = new SalesDashboardQueries();

    @Test
    void businessScopeAppliesTenantOriginAndStoreAsSourceFilters() {
        SalesDashboardScope scope = new SalesDashboardScope(
            "INTERNAL", "sales", 1L, "INTERNAL", 31L, "Internal Sales");

        var quote = queries.quote(scope);
        var order = queries.order(scope);

        assertThat(quote.getSqlSegment()).contains("tenant_id", "business_origin", "sales_store_id");
        assertThat(order.getSqlSegment()).contains("tenant_id", "business_origin", "sales_store_id");
        assertThat(quote.getParamNameValuePairs()).containsValues(1L, "INTERNAL", 31L);
        assertThat(order.getParamNameValuePairs()).containsValues(1L, "INTERNAL", 31L);
    }

    @Test
    void platformScopeLeavesTenantAndOriginOpenButKeepsDataPermissionMappers() {
        SalesDashboardScope scope = new SalesDashboardScope("PLATFORM", "ops", null, null, null, null);

        assertThat(queries.order(scope).getSqlSegment()).doesNotContain("tenant_id", "business_origin", "sales_store_id");
        assertDataScope(SalesDashboardQuoteMapper.class);
        assertDataScope(SalesDashboardOrderMapper.class);
    }

    private void assertDataScope(Class<?> mapperType) {
        DataPermission permission = mapperType.getAnnotation(DataPermission.class);
        Set<String> keys = Arrays.stream(permission.value()).flatMap(column -> Arrays.stream(column.key()))
            .collect(Collectors.toSet());
        assertThat(keys).contains("deptName", "userName");
    }
}
