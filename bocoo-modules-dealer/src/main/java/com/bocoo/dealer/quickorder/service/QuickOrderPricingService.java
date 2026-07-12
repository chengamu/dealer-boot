package com.bocoo.dealer.quickorder.service;

import com.bocoo.dealer.quickorder.domain.bo.QuickOrderItemBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;

public interface QuickOrderPricingService {
    QuickOrderItemVo calculateItem(Long quickOrderId, QuickOrderItemBo bo);

    QuickOrderVo calculateAll(Long quickOrderId);
}
