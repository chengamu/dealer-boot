package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.CustomerQuoteConversionSnapshot;
import com.bocoo.merchant.service.CustomerQuoteConversionSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerQuoteConversionSupportImpl extends MerchantServiceSupport implements CustomerQuoteConversionSupport {
    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemMapper itemMapper;

    @Override
    public CustomerQuoteConversionSnapshot load(Long quoteId) {
        CustomerQuote quote = quoteMapper.selectOne(this.<CustomerQuote>activeQuery()
            .eq("tenant_id", currentTenantId()).eq("quote_id", quoteId), false);
        if (quote == null) throw ServiceException.ofMessageKey("customer.quote.notFound");
        var items = itemMapper.selectList(this.<CustomerQuoteItem>activeQuery()
            .eq("tenant_id", quote.getTenantId()).eq("quote_id", quoteId).orderByAsc("line_no", "sort_order"));
        return new CustomerQuoteConversionSnapshot(quote, items);
    }

    @Override
    public boolean markConverted(Long quoteId, Long salesDocumentId, String orderNo) {
        return quoteMapper.update(null, new UpdateWrapper<CustomerQuote>()
            .eq("quote_id", quoteId)
            .eq("tenant_id", currentTenantId())
            .eq("status", "CONFIRMED")
            .isNull("sales_document_id")
            .set("sales_document_id", salesDocumentId)
            .set("order_no", orderNo)
            .set("converted_by_id", LoginHelper.getUserId())
            .set("converted_by", LoginHelper.getUsername())
            .set("converted_time", TimeUtils.utcNow())) > 0;
    }
}
