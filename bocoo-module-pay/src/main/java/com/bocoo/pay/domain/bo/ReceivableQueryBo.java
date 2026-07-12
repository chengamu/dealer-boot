package com.bocoo.pay.domain.bo;

import lombok.Data;

@Data
public class ReceivableQueryBo {
    private Long merchantId;
    private String merchantName;
    private String salesOrderNo;
    private String payOrderNo;
    private String status;
}
