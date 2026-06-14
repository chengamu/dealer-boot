package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ConfigTemplateVersion;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配置模板版本视图对象
 */
@Data
@AutoMapper(target = ConfigTemplateVersion.class)
@Schema(description = "配置模板版本视图对象")
public class ConfigTemplateVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置模板版本ID
     */
    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    /**
     * 配置模板ID
     */
    @Schema(description = "配置模板ID")
    private Long templateId;

    /**
     * 配置模板编码
     */
    @Schema(description = "配置模板编码")
    private String templateCode;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String versionNo;

    /**
     * 版本名称
     */
    @Schema(description = "版本名称")
    private String versionName;

    /**
     * 版本状态：DRAFT草稿，SUBMITTED已提交，PUBLISHED已发布，ARCHIVED已归档
     */
    @Schema(description = "版本状态：DRAFT草稿，SUBMITTED已提交，PUBLISHED已发布，ARCHIVED已归档")
    private String versionStatus;

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
     * 销售产品ID
     */
    @Schema(description = "销售产品ID")
    private Long salesProductId;

    /**
     * 销售产品编码
     */
    @Schema(description = "销售产品编码")
    private String salesProductCode;

    /**
     * 默认销售变体ID
     */
    @Schema(description = "默认销售变体ID")
    private Long salesVariantId;

    /**
     * 默认销售变体编码
     */
    @Schema(description = "默认销售变体编码")
    private String salesVariantCode;

    /**
     * 引用价格方案版本ID
     */
    @Schema(description = "引用价格方案版本ID")
    private Long pricePlanVersionId;

    /**
     * 引用价格方案编码
     */
    @Schema(description = "引用价格方案编码")
    private String pricePlanCode;

    /**
     * 模板结构快照JSON
     */
    @Schema(description = "模板结构快照JSON")
    private String schemaJson;

    /**
     * 草稿内容哈希
     */
    @Schema(description = "草稿内容哈希")
    private String draftHash;

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
     * 发布后生成的产品发布包ID
     */
    @Schema(description = "发布后生成的产品发布包ID")
    private Long publishedPackageId;

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
