package com.bocoo.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProductFormulaSimulationItemVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String materialTypeNameCn;
    private String attributeGroupNameCn;
    private String specModelText;
    private String unitCode;
    private BigDecimal usageQty;
    private BigDecimal lossRate;
    private BigDecimal unitPrice;
    private BigDecimal salesPrice;
    private BigDecimal amount;
    private String usageSummary;
    private String productionRemark;
}
