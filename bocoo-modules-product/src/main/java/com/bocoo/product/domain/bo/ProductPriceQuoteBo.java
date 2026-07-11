package com.bocoo.product.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ProductPriceQuoteBo {
    private BigDecimal orderWidth;
    private BigDecimal orderHeight;
    private Integer orderQuantity = 1;
    private Map<String, String> selectedOptionValues = new LinkedHashMap<>();
}
