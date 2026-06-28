package com.bocoo.ai.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiUserQuotaBo {
    private Long quotaId;
    private Long tenantId;
    private Long userId;
    private Long dailyRequestLimit;
    private Long dailyTokenLimit;
    private BigDecimal dailyCostLimit;
    private String status;
}
