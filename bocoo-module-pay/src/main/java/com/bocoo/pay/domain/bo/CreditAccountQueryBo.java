package com.bocoo.pay.domain.bo;

import lombok.Data;

@Data
public class CreditAccountQueryBo {
    private String businessOrigin;
    private Long tenantId;
    private Long salesStoreId;
    private Long merchantId;
    private String merchantName;
    private String currency;
    private String status;
}
