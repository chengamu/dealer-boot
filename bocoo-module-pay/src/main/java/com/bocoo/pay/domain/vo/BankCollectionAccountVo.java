package com.bocoo.pay.domain.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BankCollectionAccountVo {
    String bankAccountId;
    String bankName;
    String accountName;
    String accountNumber;
    String accountNumberMasked;
    String swiftCode;
    String routingNumber;
    String currency;
    String remark;
}
