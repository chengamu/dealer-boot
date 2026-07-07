package com.bocoo.system.domain.vo;

import java.math.BigDecimal;

public record MerchantProfileLevelSnapshot(
    Long levelId,
    String levelCode,
    String levelName,
    BigDecimal defaultDiscountRate,
    BigDecimal defaultCreditLimit
) {
}
