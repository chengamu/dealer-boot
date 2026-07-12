package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class CreditAccountSummaryVo {
    Long creditAccountId;
    Long tenantId;
    Long merchantId;
    String merchantName;
    BigDecimal creditLimit;
    BigDecimal usedCredit;
    BigDecimal availableCredit;
    String currency;
    String status;
    String frozenReason;
    LocalDateTime updateTime;

    public static CreditAccountSummaryVo from(MerchantCreditAccount row) {
        if (row == null) return null;
        return builder().creditAccountId(row.getCreditAccountId()).tenantId(row.getTenantId())
            .merchantId(row.getMerchantId()).merchantName(row.getMerchantName())
            .creditLimit(row.getCreditLimit()).usedCredit(row.getUsedCredit())
            .availableCredit(row.getCreditLimit().subtract(row.getUsedCredit()))
            .currency(row.getCurrency()).status(row.getStatus()).frozenReason(row.getFrozenReason())
            .updateTime(row.getUpdateTime()).build();
    }
}
