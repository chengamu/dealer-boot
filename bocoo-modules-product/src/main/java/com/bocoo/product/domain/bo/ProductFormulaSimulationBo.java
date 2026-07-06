package com.bocoo.product.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ProductFormulaSimulationBo {
    private BigDecimal orderWidth;
    private BigDecimal orderHeight;
    private Integer orderQuantity;
    private String room;
    private Map<String, String> selectedOptionValues = new LinkedHashMap<>();
    private Boolean saveResult;
    private String remark;
}
