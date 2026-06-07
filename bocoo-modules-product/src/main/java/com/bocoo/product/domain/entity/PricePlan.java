package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 价格方案表 pc_price_plan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_plan")
@Schema(description = "价格方案表")
public class PricePlan extends BaseEntity {

    /**
     * 价格方案ID
     */
    @TableId(value = "price_plan_id")
    @Schema(description = "价格方案ID")
    private Long pricePlanId;

    /**
     * 价格方案编码
     */
    @Schema(description = "价格方案编码")
    private String pricePlanCode;

    /**
     * 价格方案中文名称
     */
    @Schema(description = "价格方案中文名称")
    private String pricePlanNameCn;

    /**
     * 价格方案英文名称
     */
    @Schema(description = "价格方案英文名称")
    private String pricePlanNameEn;

    /**
     * 产品模型ID
     */
    @Schema(description = "产品模型ID")
    private Long productModelId;

    /**
     * 产品模型编码
     */
    @Schema(description = "产品模型编码")
    private String productModelCode;

    /**
     * 销售变体ID
     */
    @Schema(description = "销售变体ID")
    private Long salesVariantId;

    /**
     * 销售变体编码
     */
    @Schema(description = "销售变体编码")
    private String salesVariantCode;

    /**
     * 币种编码
     */
    @Schema(description = "币种编码")
    private String currencyCode;

    /**
     * 计价模式：FIXED固定价，MATRIX矩阵价，AREA面积价，OPTION_ADDER选项加价
     */
    @Schema(description = "计价模式：FIXED固定价，MATRIX矩阵价，AREA面积价，OPTION_ADDER选项加价")
    private String pricingMode;

    /**
     * 当前版本ID
     */
    @Schema(description = "当前版本ID")
    private Long currentVersionId;

    /**
     * 当前版本号
     */
    @Schema(description = "当前版本号")
    private String currentVersionNo;

    /**
     * 当前已发布版本ID
     */
    @Schema(description = "当前已发布版本ID")
    private Long publishedVersionId;

    /**
     * 当前已发布版本号
     */
    @Schema(description = "当前已发布版本号")
    private String publishedVersionNo;

    /**
     * 业务状态：DRAFT草稿，PUBLISHED已发布，ARCHIVED已归档
     */
    @Schema(description = "业务状态：DRAFT草稿，PUBLISHED已发布，ARCHIVED已归档")
    private String bizStatus;

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
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
