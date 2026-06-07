package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品物料业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductMaterial.class, reverseConvertGenerate = false)
@Schema(description = "产品物料业务对象")
public class ProductMaterialBo extends BaseBo {

    /**
     * 产品物料ID
     */
    @Schema(description = "产品物料ID")
    private Long materialId;

    /**
     * 物料编码
     */
    @Schema(description = "物料编码")
    private String materialCode;

    /**
     * 物料中文名称
     */
    @Schema(description = "物料中文名称")
    private String materialNameCn;

    /**
     * 物料英文名称
     */
    @Schema(description = "物料英文名称")
    private String materialNameEn;

    /**
     * 物料类型
     */
    @Schema(description = "物料类型")
    private String materialType;

    /**
     * 业务口径类型
     */
    @Schema(description = "业务口径类型")
    private String businessType;

    /**
     * 单位编码
     */
    @Schema(description = "单位编码")
    private String unitCode;

    /**
     * 供应商名称
     */
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 删除标志：0存在，2删除
     */
    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    /**
     * 物料属性JSON
     */
    @Schema(description = "物料属性JSON")
    private String attributeJson;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
