package com.bocoo.dealer.quickorder.runtime;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.system.domain.vo.MerchantProfileVo;

public record QuickOrderPricingResult(QuickOrderItem item, String currencyCode, MerchantProfileVo profile) {
}
