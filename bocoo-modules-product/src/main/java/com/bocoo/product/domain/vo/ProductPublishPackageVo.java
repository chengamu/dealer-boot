package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPublishPackage;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品发布包视图对象
 */
@Data
@AutoMapper(target = ProductPublishPackage.class)
@Schema(description = "产品发布包视图对象")
public class ProductPublishPackageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品发布包ID
     */
    @Schema(description = "产品发布包ID")
    private Long packageId;

    /**
     * 发布包编码
     */
    @Schema(description = "发布包编码")
    private String packageCode;

    /**
     * 发布包类型：PRODUCT产品能力发布包
     */
    @Schema(description = "发布包类型：PRODUCT产品能力发布包")
    private String packageType;

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
     * 发布包状态：PUBLISHED已发布，ARCHIVED已归档
     */
    @Schema(description = "发布包状态：PUBLISHED已发布，ARCHIVED已归档")
    private String packageStatus;

    /**
     * 发布包内容哈希
     */
    @Schema(description = "发布包内容哈希")
    private String packageHash;

    /**
     * 发布包完整快照JSON
     */
    @Schema(description = "发布包完整快照JSON")
    private String packageJson;

    /**
     * 版本摘要快照JSON
     */
    @Schema(description = "版本摘要快照JSON")
    private String versionSnapshotJson;

    /**
     * 生效开始时间，UTC语义
     */
    @Schema(description = "生效开始时间，UTC语义")
    private LocalDateTime effectiveFrom;

    /**
     * 生效结束时间，UTC语义
     */
    @Schema(description = "生效结束时间，UTC语义")
    private LocalDateTime effectiveTo;

    /**
     * 发布时间，UTC语义
     */
    @Schema(description = "发布时间，UTC语义")
    private LocalDateTime publishedTime;

    /**
     * 发布人用户ID
     */
    @Schema(description = "发布人用户ID")
    private Long publishedById;

    /**
     * 发布人名称
     */
    @Schema(description = "发布人名称")
    private String publishedByName;

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
