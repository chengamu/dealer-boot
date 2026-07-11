package com.bocoo.dealer.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerQuoteOrderLinePreviewVo {
    private Long quoteItemId;
    private Integer lineNo;
    private String roomLocation;
    private String saleProductName;
    private Integer quantity;
    private BigDecimal listAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal lineAmount;
}
