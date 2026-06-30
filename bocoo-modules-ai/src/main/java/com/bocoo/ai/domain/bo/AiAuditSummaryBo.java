package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiAuditSummary;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiAuditSummary.class, reverseConvertGenerate = false)
@Schema(description = "AI 审计摘要查询对象")
public class AiAuditSummaryBo extends BaseBo {

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "动作类型")
    private String actionType;

    @Schema(description = "工具编码")
    private String toolCode;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "审批状态")
    private String approvalStatus;

    @Schema(description = "请求ID")
    private String requestId;
}
