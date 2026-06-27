package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaOptionMaterial.class, reverseConvertGenerate = false)
public class ProductFormulaOptionMaterialBo extends BaseBo {
    private Long optionMaterialId;
    private Long tenantId;
    private Long formulaId;
    private Long optionId;
    private Long optionValueId;
    private String optionCode;
    private String valueCode;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private Boolean requiredFlag;
    private Boolean defaultFlag;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
