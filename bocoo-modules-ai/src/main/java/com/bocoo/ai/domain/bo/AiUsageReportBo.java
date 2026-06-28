package com.bocoo.ai.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiUsageReportBo {
    private Long tenantId;
    private Long userId;
    private String sessionId;
    private String requestId;
    private String providerCallId;
    private String provider;
    private String model;
    private Long inputTokens;
    private Long outputTokens;
    private BigDecimal costAmount;
    private Long latencyMs;
    private String status;
}
