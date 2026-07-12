package com.bocoo.merchant.service;

import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;

public interface CustomerQuoteDraftService {
    CustomerQuoteVo insert(CustomerQuoteBo bo);

    CustomerQuoteVo update(CustomerQuoteBo bo);

    Boolean delete(Long[] ids);

    Long copy(Long id);

    CustomerQuoteItemVo calculateItem(CustomerQuoteItemBo bo, String quoteLanguage);

}
