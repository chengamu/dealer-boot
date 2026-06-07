package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ConfigOption;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配置答案选项视图对象
 */
@Data
@AutoMapper(target = ConfigOption.class)
@Schema(description = "配置答案选项视图对象")
public class ConfigOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置答案选项ID
     */
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
