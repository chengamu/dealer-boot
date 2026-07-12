package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CustomerQuoteDraftAssembler {
    private final CustomerQuoteItemFactory itemFactory;

    CustomerQuoteVo assemble(CustomerQuote quote, CustomerQuoteWriteResult result) {
        CustomerQuoteVo vo = MapstructUtils.convert(quote, CustomerQuoteVo.class);
        if (vo == null) return new CustomerQuoteVo();
        vo.setItems(result.items().stream().map(itemFactory::toVo).toList());
        vo.setItemCount(vo.getItems().size());
        return vo;
    }
}
