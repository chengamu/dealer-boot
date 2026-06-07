package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品模型表 pc_product_model
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_product_model")
@Schema(description = "产品模型表")
public class ProductModel extends BaseEntity {

    /**
     * 产品模型ID
     */
    @TableId(value = "model_id")
    @Schema(description = "产品模型ID")
    private Long modelId;

    /**
     * 产品模型编码
     */
    @Schema(description = "产品模型编码")
    private String modelCode;

    /**
     * 产品模型中文名称
     */
    @Schema(description = "产品模型中文名称")
    private String modelNameCn;

    /**
     * 产品模型英文名称
     */
    @Schema(description = "产品模型英文名称")
    private String modelNameEn;

    /**
     * 产品分类ID
     */
    @Schema(description = "产品分类ID")
    private Long categoryId;

    /**
     * 产品分类编码快照
     */
    @Schema(description = "产品分类编码快照")
    private String categoryCode;

    /**
     * 产品分类中文名称快照
     */
    @Schema(description = "产品分类中文名称快照")
    private String categoryNameCn;

    /**
     * 产品族
     */
    @Schema(description = "产品族")
    private String productFamily;

    /**
     * 结构类型
     */
    @Schema(description = "结构类型")
    private String structureType;

    /**
     * 销售模式
     */
    @Schema(description = "销售模式")
    private String salesMode;

    /**
     * 产品性质
     */
    @Schema(description = "产品性质")
    private String productNature;

    /**
     * 单位编码
     */
    @Schema(description = "单位编码")
    private String unitCode;

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
     * 发布状态
     */
    @Schema(description = "发布状态")
    private String publishStatus;

    /**
     * 产品负责人用户ID
     */
    @Schema(description = "产品负责人用户ID")
    private Long ownerUserId;

    /**
     * 资料负责人用户ID
     */
    @Schema(description = "资料负责人用户ID")
    private Long assetOwnerUserId;

    /**
     * 价格负责人用户ID
     */
    @Schema(description = "价格负责人用户ID")
    private Long priceOwnerUserId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
