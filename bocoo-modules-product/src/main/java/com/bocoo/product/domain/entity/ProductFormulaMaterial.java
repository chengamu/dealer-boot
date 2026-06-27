package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配方原料池 pc_formula_material
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_material")
@Schema(description = "配方原料池")
public class ProductFormulaMaterial extends BaseEntity {

    @TableId(value = "formula_material_id")
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
