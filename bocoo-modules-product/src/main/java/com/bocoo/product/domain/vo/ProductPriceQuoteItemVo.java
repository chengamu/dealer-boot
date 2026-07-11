package com.bocoo.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductPriceQuoteItemVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long priceMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String attributeGroupNameCn;
    private String unitCode;
    private BigDecimal usageQty;
    private BigDecimal unitPrice;
    private String matchedConditionText;
    private String priceFormula;
    private BigDecimal amount;
}
