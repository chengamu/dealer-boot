package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ConfigQuestion;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配置问题视图对象
 */
@Data
@AutoMapper(target = ConfigQuestion.class)
@Schema(description = "配置问题视图对象")
public class ConfigQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 配置问题组ID
     */
    @Schema(description = "配置问题组ID")
    private Long questionGroupId;

    /**
     * 配置问题编码
     */
    @Schema(description = "配置问题编码")
    private String questionCode;

    /**
     * 配置问题中文名称
     */
    @Schema(description = "配置问题中文名称")
    private String questionNameCn;

    /**
     * 配置问题英文名称
     */
    @Schema(description = "配置问题英文名称")
    private String questionNameEn;

    /**
     * 中文帮助说明
     */
    @Schema(description = "中文帮助说明")
    private String helpTextCn;

    /**
     * 英文帮助说明
     */
    @Schema(description = "英文帮助说明")
    private String helpTextEn;

    /**
     * 输入类型
     */
    @Schema(description = "输入类型")
    private String inputType;

    /**
     * 是否必填：1是，0否
     */
    @Schema(description = "是否必填：1是，0否")
    private String requiredFlag;

    /**
     * 是否客户可见：1是，0否
     */
    @Schema(description = "是否客户可见：1是，0否")
    private String customerVisible;

    /**
     * 默认值
     */
    @Schema(description = "默认值")
    private String defaultValue;

    /**
     * 校验规则JSON
     */
    @Schema(description = "校验规则JSON")
    private String validationJson;

    /**
     * 展示规则摘要JSON
     */
    @Schema(description = "展示规则摘要JSON")
    private String displayRuleJson;

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
