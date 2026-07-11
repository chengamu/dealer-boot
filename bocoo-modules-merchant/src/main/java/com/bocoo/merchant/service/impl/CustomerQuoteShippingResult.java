package com.bocoo.merchant.service.impl;

import java.math.BigDecimal;

record CustomerQuoteShippingResult(
    Long templateId,
    String templateCode,
    Long ruleId,
    String feeCode,
    BigDecimal unitAmount
) {
}
