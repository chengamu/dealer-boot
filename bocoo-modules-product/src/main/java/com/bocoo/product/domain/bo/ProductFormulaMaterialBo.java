package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaMaterial.class, reverseConvertGenerate = false)
public class ProductFormulaMaterialBo extends BaseBo {
    private Long formulaMaterialId;
    private Long tenantId;
    private Long formulaId;
    private Integer lineNo;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String specModelText;
    private Long attributeGroupId;
    private String attributeGroupCode;
    private String attributeGroupNameCn;
    private Long materialTypeId;
    private String materialTypeCode;
    private String materialTypeNameCn;
    private String unitCode;
    private Boolean defaultFlag;
    private Boolean requiredFlag;
    private String usageMode;
    private String usageFormula;
    private BigDecimal fixedUsageQty;
    private String calculationUnitCode;
    private String roundingMode;
    private BigDecimal minUsageQty;
    private BigDecimal maxUsageQty;
    private BigDecimal lossRate;
    private String productionRemark;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
