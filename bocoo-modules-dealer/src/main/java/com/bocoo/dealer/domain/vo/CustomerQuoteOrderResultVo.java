package com.bocoo.dealer.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CustomerQuoteOrderResultVo {
    private Long salesDocumentId;
    private String orderNo;
    private BigDecimal totalAmount;
}
