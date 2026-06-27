package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductFormulaMaterial.class)
public class ProductFormulaMaterialVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
