package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配方选项值关联物料 pc_formula_option_material
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_formula_option_material")
@Schema(description = "配方选项值关联物料")
public class ProductFormulaOptionMaterial extends BaseEntity {

    @TableId(value = "option_material_id")
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
