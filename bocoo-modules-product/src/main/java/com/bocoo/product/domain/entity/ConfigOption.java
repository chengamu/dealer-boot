package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置答案选项表 pc_config_option
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_config_option")
@Schema(description = "配置答案选项表")
public class ConfigOption extends BaseEntity {

    /**
     * 配置答案选项ID
     */
    @TableId(value = "option_id")
    @Schema(description = "配置答案选项ID")
    private Long optionId;

    /**
     * 配置问题ID
     */
    @Schema(description = "配置问题ID")
    private Long questionId;

    /**
     * 配置模板版本ID
     */
    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    /**
     * 答案选项编码
     */
    @Schema(description = "答案选项编码")
    private String optionCode;

    /**
     * 答案选项中文名称
     */
    @Schema(description = "答案选项中文名称")
    private String optionNameCn;

    /**
     * 答案选项英文名称
     */
    @Schema(description = "答案选项英文名称")
    private String optionNameEn;

    /**
     * 答案选项值
     */
    @Schema(description = "答案选项值")
    private String optionValue;

    /**
     * 来源类型
     */
    @Schema(description = "来源类型")
    private String sourceType;

    /**
     * 来源对象ID
     */
    @Schema(description = "来源对象ID")
    private Long sourceRefId;

    /**
     * 来源编码
     */
    @Schema(description = "来源编码")
    private String sourceCode;

    /**
     * 来源名称
     */
    @Schema(description = "来源名称")
    private String sourceName;

    /**
     * 客户展示中文名称
     */
    @Schema(description = "客户展示中文名称")
    private String displayNameCn;

    /**
     * 客户展示英文名称
     */
    @Schema(description = "客户展示英文名称")
    private String displayNameEn;

    /**
     * 客户展示值编码
     */
    @Schema(description = "客户展示值编码")
    private String valueCode;

    /**
     * 答案中文说明
     */
    @Schema(description = "答案中文说明")
    private String helpTextCn;

    /**
     * 答案英文说明
     */
    @Schema(description = "答案英文说明")
    private String helpTextEn;

    /**
     * 答案默认带出组件摘要JSON
     */
    @Schema(description = "答案默认带出组件摘要JSON")
    private String componentJson;

    /**
     * 答案绑定资料摘要JSON
     */
    @Schema(description = "答案绑定资料摘要JSON")
    private String mediaJson;

    /**
     * 答案价格影响摘要JSON
     */
    @Schema(description = "答案价格影响摘要JSON")
    private String priceImpactJson;

    /**
     * 轻量显示 / 禁用规则JSON
     */
    @Schema(description = "轻量显示 / 禁用规则JSON")
    private String ruleJson;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
