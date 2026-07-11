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

    CustomerQuoteVo load(Long id) {
        CustomerQuoteVo quote = quoteMapper.selectVoOne(this.<CustomerQuote>activeQuery()
            .eq("tenant_id", currentTenantId()).eq("quote_id", id), false);
        if (quote == null) {
            throw ServiceException.ofMessageKey("customer.quote.notFound");
        }
        quote.setItems(loadItems(id));
        quote.setItemCount(quote.getItems().size());
        return quote;
    }

    private List<CustomerQuoteItemVo> loadItems(Long quoteId) {
        return itemMapper.selectList(activeItems(quoteId).orderByAsc("line_no", "sort_order"))
            .stream().map(calculator::toVo).toList();
    }

    private QueryWrapper<CustomerQuoteItem> activeItems(Long quoteId) {
        return this.<CustomerQuoteItem>activeQuery()
            .eq("tenant_id", currentTenantId()).eq("quote_id", quoteId);
    }
}
