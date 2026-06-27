package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品物料表 pc_material
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_material")
@Schema(description = "产品物料表")
public class ProductMaterial extends BaseEntity {

    /**
     * 产品物料ID
     */
    @TableId(value = "material_id")
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

    @TableField(exist = false)
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

    @Schema(description = "销售价")
    private BigDecimal salesPrice;

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

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
