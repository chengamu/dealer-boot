package com.bocoo.ai.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AiUserQuotaVo {
    private Long quotaId;
    private Long tenantId;
    private Long userId;
    private Long dailyRequestLimit;
    private Long dailyTokenLimit;
    private BigDecimal dailyCostLimit;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
