package com.bocoo.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductPriceQuoteVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long saleProductId;
    private Long formulaVersionId;
    private String currencyCode;
    private Integer orderQuantity;
    private BigDecimal singleAmount;
    private BigDecimal totalAmount;
    private List<ProductPriceQuoteItemVo> items = new ArrayList<>();
}
