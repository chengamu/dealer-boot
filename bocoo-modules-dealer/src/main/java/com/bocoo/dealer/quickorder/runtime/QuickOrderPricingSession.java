package com.bocoo.dealer.quickorder.runtime;

import com.bocoo.system.domain.vo.MerchantProfileVo;

import java.math.BigDecimal;
import java.util.Map;

public record QuickOrderPricingSession(MerchantProfileVo profile, BigDecimal fallbackRate,
                                       Map<String, BigDecimal> rates) {
}
