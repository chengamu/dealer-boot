package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiUserQuota;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiUserQuota.class)
public class AiUserQuotaVo {
    private Long quotaId;
    private Long tenantId;
    private Long userId;
    private String userName;
    private String nickName;
    private Long dailyRequestLimit;
    private Long dailyTokenLimit;
    private BigDecimal dailyCostLimit;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
