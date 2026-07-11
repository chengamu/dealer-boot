package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPriceMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = ProductPriceMaterial.class)
public class ProductPriceMaterialVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long priceMaterialId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String specModelText;
    private String attributeGroupCode;
    private String attributeGroupNameCn;
    private String materialTypeCode;
    private String materialTypeNameCn;
    private String unitCode;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
    private Integer ruleCount;
    private Boolean defaultRuleFlag;
}
