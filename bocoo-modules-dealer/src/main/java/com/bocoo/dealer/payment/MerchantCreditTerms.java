package com.bocoo.dealer.payment;

import java.math.BigDecimal;

record MerchantCreditTerms(Long merchantId, String merchantName,
                           BigDecimal creditLimit, Integer creditTermDays) {
}
