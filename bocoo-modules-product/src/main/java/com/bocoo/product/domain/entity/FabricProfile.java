package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 面料资料表 pc_fabric_profile
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_fabric_profile")
@Schema(description = "面料资料表")
public class FabricProfile extends BaseEntity {

    @TableId(value = "profile_id")
    @Schema(description = "面料资料ID")
    private Long fabricId;

    @Schema(description = "面料编码")
    private String fabricCode;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料中文名称快照")
    private String materialNameCn;

    @Schema(description = "系列ID")
    private Long seriesId;

    @Schema(description = "系列编码快照")
    private String seriesCode;

    @Schema(description = "系列中文名快照")
    private String seriesNameCn;

    @Schema(description = "颜色编码")
    private String colorCode;

    @Schema(description = "颜色名称")
    private String colorName;

    @Schema(description = "材质成分")
    private String materialComposition;

    @Schema(description = "纹理类型")
    private String textureType;

    @Schema(description = "表面处理")
    private String finishType;

    @Schema(description = "工厂型号")
    private String factoryModel;

    @Schema(description = "样册编号")
    private String sampleBookNo;

    @Schema(description = "供应商料号")
    private String vendorItemNo;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "门幅值")
    private BigDecimal widthValue;

    @Schema(description = "门幅单位")
    private String widthUnit;

    @Schema(description = "厚度值")
    private BigDecimal thicknessValue;

    @Schema(description = "厚度单位")
    private String thicknessUnit;

    @Schema(description = "克重")
    private BigDecimal gsmValue;

    @Schema(description = "采购单位编码")
    private String purchaseUnitCode;

    @Schema(description = "库存单位编码")
    private String inventoryUnitCode;

    @Schema(description = "销售单位编码")
    private String salesUnitCode;

    @Schema(description = "原始属性文本")
    private String legacyAttributeText;

    @Schema(description = "老系统来源")
    private String legacySource;

    @Schema(description = "老系统编号")
    private String legacyId;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
