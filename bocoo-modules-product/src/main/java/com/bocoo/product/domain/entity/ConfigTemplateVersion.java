package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 配置模板版本表 pc_config_template_version
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_config_template_version")
@Schema(description = "配置模板版本表")
public class ConfigTemplateVersion extends BaseEntity {

    /**
     * 配置模板版本ID
     */
    @TableId(value = "template_version_id")
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

}
