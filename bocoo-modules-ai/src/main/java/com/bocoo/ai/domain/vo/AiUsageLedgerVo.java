package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiUsageLedger;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiUsageLedger.class)
public class AiUsageLedgerVo {
    private Long usageId;
    private Long tenantId;
    private Long userId;
    private String userName;
    private String nickName;
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
