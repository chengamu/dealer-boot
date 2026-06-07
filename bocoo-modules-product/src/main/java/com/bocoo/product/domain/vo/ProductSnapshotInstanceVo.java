package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductSnapshotInstance;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品能力快照实例视图对象
 */
@Data
@AutoMapper(target = ProductSnapshotInstance.class)
@Schema(description = "产品能力快照实例视图对象")
public class ProductSnapshotInstanceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品能力快照实例ID
     */
    @Schema(description = "产品能力快照实例ID")
    private Long snapshotId;

    /**
     * 来源系统：ORDER订单，ERP，MES
     */
    @Schema(description = "来源系统：ORDER订单，ERP，MES")
    private String sourceSystem;

    /**
     * 来源业务类型：ORDER_LINE订单行，QUOTE_LINE报价行，MES_PLAN生产计划
     */
    @Schema(description = "来源业务类型：ORDER_LINE订单行，QUOTE_LINE报价行，MES_PLAN生产计划")
    private String sourceBizType;

    /**
     * 来源业务单号
     */
    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    /**
     * 来源业务行号
     */
    @Schema(description = "来源业务行号")
    private String sourceBizLineNo;

    /**
     * 客户编码
     */
    @Schema(description = "客户编码")
    private String customerCode;

    /**
     * 发布包ID
     */
    @Schema(description = "发布包ID")
    private Long packageId;

    /**
     * 发布包编码
     */
    @Schema(description = "发布包编码")
    private String packageCode;

    /**
     * 发布包哈希
     */
    @Schema(description = "发布包哈希")
    private String packageHash;

    /**
     * 产品模型编码
     */
    @Schema(description = "产品模型编码")
    private String productModelCode;

    /**
     * 销售变体编码
     */
    @Schema(description = "销售变体编码")
    private String salesVariantCode;

    /**
     * 配置模板版本ID
     */
    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    /**
     * 配置模板版本号
     */
    @Schema(description = "配置模板版本号")
    private String templateVersionNo;

    /**
     * 价格方案版本ID
     */
    @Schema(description = "价格方案版本ID")
    private Long pricePlanVersionId;

    /**
     * 价格方案编码
     */
    @Schema(description = "价格方案编码")
    private String pricePlanCode;

    /**
     * 用户已选答案JSON
     */
    @Schema(description = "用户已选答案JSON")
    private String selectedOptionsJson;

    /**
     * 尺寸和输入参数JSON
     */
    @Schema(description = "尺寸和输入参数JSON")
    private String inputValuesJson;

    /**
     * 产品能力快照JSON
     */
    @Schema(description = "产品能力快照JSON")
    private String snapshotJson;

    /**
     * 产品能力快照哈希
     */
    @Schema(description = "产品能力快照哈希")
    private String snapshotHash;

    /**
     * 快照状态：BUILT已构建，VOID作废
     */
    @Schema(description = "快照状态：BUILT已构建，VOID作废")
    private String snapshotStatus;

    /**
     * 快照构建时间，UTC语义
     */
    @Schema(description = "快照构建时间，UTC语义")
    private LocalDateTime builtTime;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间，UTC语义
     */
    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createTime;

    /**
     * 更新时间，UTC语义
     */
    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updateTime;

}
