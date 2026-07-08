package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AutoMapper(target = ProductPriceFabricRule.class)
public class ProductPriceFabricRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long fabricRuleId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String unitCode;
    private String optionCombinationKey;
    private String optionCombinationName;
    private String priceMode;
    private BigDecimal basePrice;
    private String areaFormula;
    private BigDecimal minBillArea;
    private BigDecimal lossRate;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
