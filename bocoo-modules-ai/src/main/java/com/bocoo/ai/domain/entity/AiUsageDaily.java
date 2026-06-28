package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("ai_usage_daily")
public class AiUsageDaily {
    private Long tenantId;
    private Long userId;
    private LocalDate usageDate;
    private Long requestCount;
    private Long inputTokens;
    private Long outputTokens;
    private BigDecimal costAmount;
}
