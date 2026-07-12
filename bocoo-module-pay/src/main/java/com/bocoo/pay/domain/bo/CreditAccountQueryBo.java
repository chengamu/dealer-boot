package com.bocoo.pay.domain.bo;

import lombok.Data;

@Data
public class CreditAccountQueryBo {
    private Long merchantId;
    private String merchantName;
    private String currency;
    private String status;
}
