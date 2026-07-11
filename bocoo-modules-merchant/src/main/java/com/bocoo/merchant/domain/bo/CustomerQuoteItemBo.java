package com.bocoo.merchant.domain.bo;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CustomerQuoteItemBo {
    private Long quoteItemId;
    private String roomLocation;
    private Long saleProductId;
    private BigDecimal orderWidthInch;
    private BigDecimal orderHeightInch;
    @Min(value = 1, message = "{customer.quote.quantity.invalid}")
    private Integer quantity = 1;
    private Map<String, String> selectedOptionValues = new LinkedHashMap<>();
    private Integer sortOrder;
    private String remark;
}
