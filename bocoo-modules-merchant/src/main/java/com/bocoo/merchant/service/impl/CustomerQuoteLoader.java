package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class CustomerQuoteLoader extends MerchantServiceSupport {

    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemMapper itemMapper;
    private final CustomerQuoteCalculator calculator;
    private final SalesOwnershipResolver ownershipResolver;

    CustomerQuoteVo loadBusiness(Long id) {
        CustomerQuoteVo quote = quoteMapper.selectVoOne(this.<CustomerQuote>activeQuery()
            .eq("quote_id", id)
            .eq("tenant_id", currentTenantId())
            .eq("business_origin", ownershipResolver.currentBusinessOrigin()), false);
        if (quote == null) {
            throw ServiceException.ofMessageKey("customer.quote.notFound");
        }
        quote.setItems(loadItems(quote.getTenantId(), id, false));
        quote.setItemCount(quote.getItems().size());
        return quote;
    }

    CustomerQuoteVo loadPlatform(Long id) {
        CustomerQuoteVo quote = platformIgnoreTenant(() -> quoteMapper.selectVoOne(
            this.<CustomerQuote>activeQuery().eq("quote_id", id), false));
        if (quote == null) {
            throw ServiceException.ofMessageKey("customer.quote.notFound");
        }
        quote.setItems(loadItems(quote.getTenantId(), id, true));
        quote.setItemCount(quote.getItems().size());
        return quote;
    }

    private List<CustomerQuoteItemVo> loadItems(Long tenantId, Long quoteId, boolean platformQuery) {
        java.util.function.Supplier<List<CustomerQuoteItemVo>> loader = () -> itemMapper
            .selectList(activeItems(tenantId, quoteId).orderByAsc("line_no", "sort_order"))
            .stream().map(calculator::toVo).toList();
        return platformQuery ? platformIgnoreTenant(loader) : loader.get();
    }

    private QueryWrapper<CustomerQuoteItem> activeItems(Long tenantId, Long quoteId) {
        return this.<CustomerQuoteItem>activeQuery()
            .eq("tenant_id", tenantId).eq("quote_id", quoteId);
    }
}
