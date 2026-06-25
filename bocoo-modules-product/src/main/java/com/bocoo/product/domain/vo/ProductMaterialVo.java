package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品物料视图对象
 */
@Data
@AutoMapper(target = ProductMaterial.class)
@Schema(description = "产品物料视图对象")
public class ProductMaterialVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "物料类型ID")
    private Long materialTypeId;

    @Schema(description = "物料类型编码")
    private String materialTypeCode;

    @Schema(description = "物料类型中文名称")
    private String materialTypeNameCn;

    @Schema(description = "属性分组ID")
    private Long attributeGroupId;

    @Schema(description = "属性分组编码")
    private String attributeGroupCode;

    @Schema(description = "属性分组中文名称")
    private String attributeGroupNameCn;

    @Schema(description = "物料类型编码查询别名")
    private String materialType;

    /**
     * 单位编码
     */
    @Schema(description = "单位编码")
    private String unitCode;

    @Schema(description = "副单位编码")
    private String secondaryUnitCode;

    /**
     * 厂家ID
     */
    @Schema(description = "厂家ID")
    private Long manufacturerId;

    /**
     * 厂家编码
     */
    @Schema(description = "厂家编码")
    private String manufacturerCode;

    /**
     * 厂家名称
     */
    @Schema(description = "厂家名称")
    private String manufacturerName;

    /**
     * 厂家物料编码
     */
    @Schema(description = "厂家物料编码")
    private String manufacturerItemNo;

    /**
     * 型号
     */
    @Schema(description = "型号")
    private String model;

    /**
     * 规格
     */
    @Schema(description = "规格")
    private String spec;

    @Schema(description = "规格型号展示文本")
    private String specModelText;

    /**
     * 颜色
     */
    @Schema(description = "颜色")
    private String colorName;

    /**
     * 克重/重量
     */
    @Schema(description = "克重/重量")
    private BigDecimal weightValue;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "审核人")
    private String auditBy;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 状态：ENABLED 已审核，DISABLED 未审核
     */
    @Schema(description = "状态：ENABLED 已审核，DISABLED 未审核")
    private String status;

    /**
     * 删除标志：0存在，2删除
     */
    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "物料属性值列表")
    private List<ProductMaterialAttributeVo> attributeList;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
