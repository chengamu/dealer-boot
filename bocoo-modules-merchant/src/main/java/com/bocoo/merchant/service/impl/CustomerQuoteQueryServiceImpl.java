package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemCountVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerQuoteQueryServiceImpl extends MerchantServiceSupport implements CustomerQuoteQueryService {

    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemMapper itemMapper;
    private final CustomerQuoteLoader loader;
    private final CustomerQuoteExportAssembler exportAssembler;

    @Override
    public TableDataInfo<CustomerQuoteVo> queryPageList(CustomerQuoteBo bo, PageQuery pageQuery) {
        QueryWrapper<CustomerQuote> query = activeQuery();
        CustomerQuoteReadScope scope = CustomerQuoteReadScope.current();
        applyScope(query, scope);
        if (bo != null) {
            like(query, "quote_no", bo.getQuoteNo());
            like(query, "project_name", bo.getProjectName());
            eq(query, "customer_id", bo.getCustomerId());
            eq(query, "status", bo.getStatus());
        }
        java.util.function.Supplier<TableDataInfo<CustomerQuoteVo>> queryPage = () -> page(quoteMapper, pageQuery, query,
            q -> q.orderByDesc("update_time", "create_time"));
        TableDataInfo<CustomerQuoteVo> result = scope.crossTenant() ? platformIgnoreTenant(queryPage) : queryPage.get();
        Map<QuoteKey, Integer> counts = itemCounts(result.getRows(), scope);
        result.getRows().forEach(row -> row.setItemCount(counts.getOrDefault(
            new QuoteKey(row.getTenantId(), row.getQuoteId()), 0)));
        return result;
    }

    @Override
    public CustomerQuoteVo queryById(Long id) {
        return loader.load(id);
    }

    @Override
    public List<CustomerQuoteExportCnVo> exportCn(Long id) {
        CustomerQuoteVo quote = confirmedQuote(id);
        return exportAssembler.cnRows(quote);
    }

    @Override
    public List<CustomerQuoteExportEnVo> exportEn(Long id) {
        return exportAssembler.enRows(confirmedQuote(id));
    }

    private CustomerQuoteVo confirmedQuote(Long id) {
        CustomerQuoteVo quote = queryById(id);
        if (!"CONFIRMED".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.export.confirmedOnly");
        }
        return quote;
    }

    private Map<QuoteKey, Integer> itemCounts(List<CustomerQuoteVo> rows, CustomerQuoteReadScope scope) {
        if (rows.isEmpty()) return Map.of();
        List<Long> tenantIds = rows.stream().map(CustomerQuoteVo::getTenantId).distinct().toList();
        List<Long> quoteIds = rows.stream().map(CustomerQuoteVo::getQuoteId).toList();
        java.util.function.Supplier<List<CustomerQuoteItemCountVo>> query =
            () -> itemMapper.selectItemCounts(tenantIds, quoteIds);
        List<CustomerQuoteItemCountVo> counts = scope.crossTenant() ? platformIgnoreTenant(query) : query.get();
        return counts.stream().collect(Collectors.toMap(
            item -> new QuoteKey(item.getTenantId(), item.getQuoteId()), CustomerQuoteItemCountVo::getItemCount));
    }

    private void applyScope(QueryWrapper<?> query, CustomerQuoteReadScope scope) {
        query.eq(scope.tenantId() != null, "tenant_id", scope.tenantId());
        query.eq(scope.ownerUserId() != null, "owner_user_id", scope.ownerUserId());
    }

    private record QuoteKey(Long tenantId, Long quoteId) {
    }
}
