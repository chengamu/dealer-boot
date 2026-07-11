package com.bocoo.merchant.service.impl;

import java.math.BigDecimal;

record CustomerQuoteTotals(
    String currencyCode,
    BigDecimal productAmount,
    BigDecimal shippingAmount,
    BigDecimal totalAmount,
    boolean allPassed
) {
}
