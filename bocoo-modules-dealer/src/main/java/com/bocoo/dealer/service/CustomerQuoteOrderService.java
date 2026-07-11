package com.bocoo.dealer.service;

import com.bocoo.dealer.domain.bo.CustomerQuoteConvertOrderBo;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderPreviewVo;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderResultVo;

public interface CustomerQuoteOrderService {
    CustomerQuoteOrderPreviewVo preview(Long quoteId);

    CustomerQuoteOrderResultVo convert(Long quoteId, CustomerQuoteConvertOrderBo bo);
}
