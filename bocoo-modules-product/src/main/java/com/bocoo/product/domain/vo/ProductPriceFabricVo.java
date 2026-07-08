package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPriceFabric;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = ProductPriceFabric.class)
public class ProductPriceFabricVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long priceFabricId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String unitCode;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
    private Integer ruleCount;
    private Boolean defaultRuleFlag;
}
