package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置求值结果视图对象
 */
@Data
@Schema(description = "配置求值结果视图对象")
public class ConfigEvaluationResultVo {

    @Schema(description = "销售产品ID")
    private Long salesProductId;

    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    @Schema(description = "求值状态：ALLOW允许，WARNING警告，BLOCKER阻断")
    private String resultStatus = "ALLOW";

    @Schema(description = "警告消息键或文本")
    private List<EvaluationMessageVo> warnings = new ArrayList<>();

    @Schema(description = "阻断消息键或文本")
    private List<EvaluationMessageVo> blockers = new ArrayList<>();

    @Schema(description = "可见问题列表")
    private List<ConfigQuestionVo> visibleQuestions = new ArrayList<>();

    @Schema(description = "可用答案列表")
    private List<ConfigOptionVo> availableOptions = new ArrayList<>();

    @Schema(description = "禁用答案列表")
    private List<DisabledOptionVo> disabledOptions = new ArrayList<>();

    @Schema(description = "校验结果")
    private List<EvaluationMessageVo> validations = new ArrayList<>();

    @Schema(description = "自动带出组件摘要")
    private List<Map<String, Object>> autoComponents = new ArrayList<>();

    @Schema(description = "资料资产摘要")
    private List<Map<String, Object>> mediaAssets = new ArrayList<>();

    @Data
    @Schema(description = "求值消息")
    public static class EvaluationMessageVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "消息编码")
        private String code;

        @Schema(description = "消息文本或i18n key")
        private String message;

        @Schema(description = "目标类型")
        private String targetType;

        @Schema(description = "目标ID")
        private Long targetId;
    }

    @Data
    @Schema(description = "禁用答案")
    public static class DisabledOptionVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "问题ID")
        private Long questionId;

        @Schema(description = "问题编码")
        private String questionCode;

        @Schema(description = "答案ID")
        private Long optionId;

        @Schema(description = "答案编码")
        private String optionCode;

        @Schema(description = "禁用原因")
        private String disabledReason;
    }
}
