package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ConfigRule;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配置规则视图对象
 */
@Data
@AutoMapper(target = ConfigRule.class)
@Schema(description = "配置规则视图对象")
public class ConfigRuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置规则ID
     */
    @Schema(description = "配置规则ID")
    private Long ruleId;

    /**
     * 配置模板版本ID
     */
    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    /**
     * 配置规则编码
     */
    @Schema(description = "配置规则编码")
    private String ruleCode;

    /**
     * 配置规则中文名称
     */
    @Schema(description = "配置规则中文名称")
    private String ruleNameCn;

    /**
     * 配置规则英文名称
     */
    @Schema(description = "配置规则英文名称")
    private String ruleNameEn;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型")
    private String ruleType;

    /**
     * 规则优先级
     */
    @Schema(description = "规则优先级")
    private Integer priority;

    /**
     * 规则条件JSON
     */
    @Schema(description = "规则条件JSON")
    private String conditionJson;

    /**
     * 规则动作JSON
     */
    @Schema(description = "规则动作JSON")
    private String actionJson;

    /**
     * 中文错误提示
     */
    @Schema(description = "中文错误提示")
    private String errorMessageCn;

    /**
     * 英文错误提示
     */
    @Schema(description = "英文错误提示")
    private String errorMessageEn;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

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
