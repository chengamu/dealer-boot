package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductMaterial;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
