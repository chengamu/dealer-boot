package com.bocoo.dealer.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CustomerQuoteOrderPreviewVo {
    private Long quoteId;
    private String quoteNo;
    private String customerName;
    private String projectName;
    private String merchantLevelCode;
    private String merchantLevelName;
    private String currencyCode;
    private BigDecimal customerQuoteAmount;
    private BigDecimal listAmount;
    private BigDecimal discountAmount;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal totalAmount;
    private List<CustomerQuoteOrderLinePreviewVo> items = new ArrayList<>();
}
