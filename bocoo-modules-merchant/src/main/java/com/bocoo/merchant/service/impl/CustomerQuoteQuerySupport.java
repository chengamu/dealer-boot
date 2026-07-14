package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemCountVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class CustomerQuoteQuerySupport extends MerchantServiceSupport {

    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemMapper itemMapper;

    TableDataInfo<CustomerQuoteVo> queryBusiness(CustomerQuoteBo bo, PageQuery pageQuery,
                                                 Long tenantId, String businessOrigin) {
        return pageWithCounts(buildBusinessQuery(bo, tenantId, businessOrigin), pageQuery, false);
    }

    QueryWrapper<CustomerQuote> buildBusinessQuery(CustomerQuoteBo bo, Long tenantId, String businessOrigin) {
        return buildQuery(bo).eq("tenant_id", tenantId).eq("business_origin", businessOrigin);
    }

    TableDataInfo<CustomerQuoteVo> queryPlatform(CustomerQuoteBo bo, PageQuery pageQuery) {
        return pageWithCounts(buildQuery(bo), pageQuery, true);
    }

    private TableDataInfo<CustomerQuoteVo> pageWithCounts(QueryWrapper<CustomerQuote> query,
                                                          PageQuery pageQuery, boolean platformQuery) {
        java.util.function.Supplier<TableDataInfo<CustomerQuoteVo>> loader = () -> page(
            quoteMapper, pageQuery, query, q -> q.orderByDesc("create_time", "quote_id"));
        TableDataInfo<CustomerQuoteVo> result = platformQuery ? platformIgnoreTenant(loader) : loader.get();
        Map<QuoteKey, Integer> counts = itemCounts(result.getRows(), platformQuery);
        result.getRows().forEach(row -> row.setItemCount(counts.getOrDefault(
            new QuoteKey(row.getTenantId(), row.getQuoteId()), 0)));
        return result;
    }

    private QueryWrapper<CustomerQuote> buildQuery(CustomerQuoteBo bo) {
        QueryWrapper<CustomerQuote> query = activeQuery();
        if (bo == null) return query;
        eq(query, "tenant_id", bo.getTenantId());
        eq(query, "business_origin", bo.getBusinessOrigin());
        eq(query, "sales_store_id", bo.getSalesStoreId());
        eq(query, "dept_id", bo.getDeptId());
        eq(query, "owner_user_id", bo.getOwnerUserId());
        like(query, "quote_no", bo.getQuoteNo());
        like(query, "project_name", bo.getProjectName());
        eq(query, "customer_id", bo.getCustomerId());
        eq(query, "status", bo.getStatus());
        eq(query, "sales_document_id", bo.getSalesDocumentId());
        return query;
    }

    private Map<QuoteKey, Integer> itemCounts(List<CustomerQuoteVo> rows, boolean platformQuery) {
        if (rows.isEmpty()) return Map.of();
        List<Long> tenantIds = rows.stream().map(CustomerQuoteVo::getTenantId).distinct().toList();
        List<Long> quoteIds = rows.stream().map(CustomerQuoteVo::getQuoteId).toList();
        java.util.function.Supplier<List<CustomerQuoteItemCountVo>> loader =
            () -> itemMapper.selectItemCounts(tenantIds, quoteIds);
        List<CustomerQuoteItemCountVo> counts = platformQuery ? platformIgnoreTenant(loader) : loader.get();
        return counts.stream().collect(Collectors.toMap(
            item -> new QuoteKey(item.getTenantId(), item.getQuoteId()), CustomerQuoteItemCountVo::getItemCount));
    }

    private record QuoteKey(Long tenantId, Long quoteId) {
    }
}
