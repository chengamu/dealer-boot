package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_user_quota")
public class AiUserQuota {
    @TableId(value = "quota_id")
    private Long quotaId;
    private Long tenantId;
    private Long userId;
    private Long dailyRequestLimit;
    private Long dailyTokenLimit;
    private BigDecimal dailyCostLimit;
    private String status;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
