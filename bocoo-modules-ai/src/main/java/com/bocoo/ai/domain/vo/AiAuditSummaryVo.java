package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiAuditSummary;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiAuditSummary.class)
public class AiAuditSummaryVo {
    private Long auditId;
    private Long tenantId;
    private Long userId;
    private String userName;
    private String nickName;
    private String sessionId;
    private String requestId;
    private String actionType;
    private String toolCode;
    private String businessTarget;
    private String riskLevel;
    private String approvalStatus;
    private String status;
    private LocalDateTime createdTime;
}
