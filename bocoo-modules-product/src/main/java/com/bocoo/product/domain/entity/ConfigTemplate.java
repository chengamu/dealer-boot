package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置模板表 pc_config_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_config_template")
@Schema(description = "配置模板表")
public class ConfigTemplate extends BaseEntity {

    /**
     * 配置模板ID
     */
    @TableId(value = "template_id")
    @Schema(description = "配置模板ID")
    private Long templateId;

    /**
     * 配置模板编码
     */
    @Schema(description = "配置模板编码")
    private String templateCode;

    /**
     * 配置模板中文名称
     */
    @Schema(description = "配置模板中文名称")
    private String templateNameCn;

    /**
     * 配置模板英文名称
     */
    @Schema(description = "配置模板英文名称")
    private String templateNameEn;

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
