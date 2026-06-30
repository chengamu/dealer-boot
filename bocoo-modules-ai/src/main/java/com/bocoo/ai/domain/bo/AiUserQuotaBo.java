package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiUserQuota;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiUserQuota.class, reverseConvertGenerate = false)
@Schema(description = "AI 用户额度对象")
public class AiUserQuotaBo extends BaseBo {
    @Schema(description = "额度ID")
    private Long quotaId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @NotNull(message = "ai.quota.userId.required")
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "每日请求限制")
    private Long dailyRequestLimit;

    @Schema(description = "每日 Token 限制")
    private Long dailyTokenLimit;

    @Schema(description = "每日成本限制")
    private BigDecimal dailyCostLimit;

    @Schema(description = "状态：1启用，0禁用")
    private String status;
}
