package com.bocoo.pay.domain.bo;

import lombok.Data;

@Data
public class CreditTransactionQueryBo {
    private Long creditAccountId;
    private String transactionNo;
    private String transactionType;
    private String businessNo;
}
