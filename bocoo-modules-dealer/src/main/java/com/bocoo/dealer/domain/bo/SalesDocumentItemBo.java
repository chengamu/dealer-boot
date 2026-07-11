package com.bocoo.dealer.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class SalesDocumentItemBo {
    private Long salesItemId;
    private String roomLocation;
    @NotNull
    private Long saleProductId;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal orderWidthInch;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal orderHeightInch;
    @NotNull
    @Min(1)
    private Integer quantity;
    private Map<String, String> selectedOptionValues;
    private Integer sortOrder;
    private String remark;
}
