package com.bocoo.ai.domain.bo;

import lombok.Data;

@Data
public class AiAuditReportBo {
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
}
