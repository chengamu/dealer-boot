package com.bocoo.ai.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AiQuotaVo {
    private Long dailyRequestRemaining;
    private Long dailyTokenRemaining;
    private BigDecimal dailyCostRemaining;
}
