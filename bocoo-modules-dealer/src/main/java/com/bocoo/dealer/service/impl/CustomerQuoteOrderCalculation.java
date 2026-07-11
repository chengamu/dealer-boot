package com.bocoo.dealer.service.impl;

import com.bocoo.dealer.domain.vo.CustomerQuoteOrderPreviewVo;
import com.bocoo.system.domain.vo.MerchantProfileVo;

import java.math.BigDecimal;
import java.util.Map;

record CustomerQuoteOrderCalculation(MerchantProfileVo profile, CustomerQuoteOrderPreviewVo preview,
                                     Map<Long, BigDecimal> discountRates) {
}
