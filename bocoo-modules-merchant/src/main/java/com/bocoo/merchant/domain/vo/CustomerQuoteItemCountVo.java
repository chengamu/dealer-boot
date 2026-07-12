package com.bocoo.merchant.domain.vo;

import lombok.Data;

@Data
public class CustomerQuoteItemCountVo {
    private Long tenantId;
    private Long quoteId;
    private Integer itemCount;
}
