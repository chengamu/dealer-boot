package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class CreditTransactionVo {
    Long creditTransactionId;
    Long creditAccountId;
    String transactionNo;
    String transactionType;
    String businessType;
    Long businessId;
    String businessNo;
    BigDecimal amount;
    BigDecimal beforeCreditLimit;
    BigDecimal afterCreditLimit;
    BigDecimal beforeUsedCredit;
    BigDecimal afterUsedCredit;
    String currency;
    String operatorName;
    LocalDateTime occurredTime;
    String remark;

    public static CreditTransactionVo from(MerchantCreditTransaction row) {
        return builder().creditTransactionId(row.getCreditTransactionId()).creditAccountId(row.getCreditAccountId())
            .transactionNo(row.getTransactionNo()).transactionType(row.getTransactionType())
            .businessType(row.getBusinessType()).businessId(row.getBusinessId()).businessNo(row.getBusinessNo())
            .amount(row.getAmount()).beforeCreditLimit(row.getBeforeCreditLimit()).afterCreditLimit(row.getAfterCreditLimit())
            .beforeUsedCredit(row.getBeforeUsedCredit()).afterUsedCredit(row.getAfterUsedCredit()).currency(row.getCurrency())
            .operatorName(row.getOperatorName()).occurredTime(row.getOccurredTime()).remark(row.getRemark()).build();
    }
}
