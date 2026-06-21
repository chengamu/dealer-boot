package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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

    /**
     * 物料类型
     */
    @Schema(description = "物料类型")
    private String materialType;

    @Schema(description = "面料系列ID")
    private Long fabricSeriesId;

    @Schema(description = "面料系列编码")
    private String fabricSeriesCode;

    @Schema(description = "面料系列中文名称")
    private String fabricSeriesNameCn;

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

    @Schema(description = "采购单位编码")
    private String purchaseUnitCode;

    @Schema(description = "库存单位编码")
    private String inventoryUnitCode;

    @Schema(description = "工程使用单位编码")
    private String usageUnitCode;

    /**
     * 供应商编码
     */
    @Schema(description = "供应商编码")
    private String supplierCode;

    /**
     * 供应商名称
     */
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 工厂型号
     */
    @Schema(description = "工厂型号")
    private String factoryModel;

    /**
     * 样册编号
     */
    @Schema(description = "样册编号")
    private String sampleBookNo;

    /**
     * 供应商料号
     */
    @Schema(description = "供应商料号")
    private String vendorItemNo;

    /**
     * 主规格
     */
    @Schema(description = "主规格")
    private String primarySpec;

    @Schema(description = "规格摘要")
    private String specSummary;

    /**
     * 主颜色
     */
    @Schema(description = "主颜色")
    private String primaryColor;

    /**
     * 主重量
     */
    @Schema(description = "主重量")
    private BigDecimal primaryWeight;

    @Schema(description = "是否可采购")
    private Boolean purchaseEnabled;

    @Schema(description = "是否入库")
    private Boolean inventoryEnabled;

    @Schema(description = "采购单价")
    private BigDecimal purchaseUnitPrice;

    @Schema(description = "成本单价")
    private BigDecimal costUnitPrice;

    @Schema(description = "价格币种")
    private String priceCurrencyCode;

    /**
     * 属性摘要
     */
    @Schema(description = "属性摘要")
    private String attributeSummary;

    /**
     * 老系统来源
     */
    @Schema(description = "老系统来源")
    private String legacySource;

    /**
     * 老系统编号
     */
    @Schema(description = "老系统编号")
    private String legacyId;

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
