package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_usage_ledger")
public class AiUsageLedger {
    @TableId(value = "usage_id")
    private Long usageId;
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
    private LocalDateTime createdTime;
}
