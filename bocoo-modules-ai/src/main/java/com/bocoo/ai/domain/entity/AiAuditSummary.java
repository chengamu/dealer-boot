package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_audit_summary")
public class AiAuditSummary {
    @TableId(value = "audit_id")
    private Long auditId;
    private Long tenantId;
    private Long userId;
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
