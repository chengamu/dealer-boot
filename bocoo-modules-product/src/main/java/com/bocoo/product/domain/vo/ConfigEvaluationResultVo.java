package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置求值结果视图对象
 */
@Data
@Schema(description = "配置求值结果视图对象")
public class ConfigEvaluationResultVo {

    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    @Schema(description = "求值状态：ALLOW允许，WARNING警告，BLOCKER阻断")
    private String resultStatus = "ALLOW";

    @Schema(description = "警告消息键或文本")
    private List<String> warnings = new ArrayList<>();

    @Schema(description = "阻断消息键或文本")
    private List<String> blockers = new ArrayList<>();

    @Schema(description = "可见问题列表")
    private List<ConfigQuestionVo> visibleQuestions = new ArrayList<>();

    @Schema(description = "可用答案列表")
    private List<ConfigOptionVo> availableOptions = new ArrayList<>();

    @Schema(description = "自动带出组件摘要")
    private List<Map<String, Object>> autoComponents = new ArrayList<>();
}
