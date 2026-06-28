package com.bocoo.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocoo.ai.domain.entity.AiUsageDaily;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AiUsageDailyMapper extends BaseMapper<AiUsageDaily> {

    @Insert("""
        INSERT INTO ai_usage_daily (tenant_id, user_id, usage_date, request_count, input_tokens, output_tokens, cost_amount)
        VALUES (#{tenantId}, #{userId}, #{usageDate}, 1, #{inputTokens}, #{outputTokens}, #{costAmount})
        ON CONFLICT (tenant_id, user_id, usage_date) DO UPDATE
        SET request_count = ai_usage_daily.request_count + 1,
            input_tokens = ai_usage_daily.input_tokens + EXCLUDED.input_tokens,
            output_tokens = ai_usage_daily.output_tokens + EXCLUDED.output_tokens,
            cost_amount = ai_usage_daily.cost_amount + EXCLUDED.cost_amount
        """)
    void increment(
        @Param("tenantId") Long tenantId,
        @Param("userId") Long userId,
        @Param("usageDate") LocalDate usageDate,
        @Param("inputTokens") Long inputTokens,
        @Param("outputTokens") Long outputTokens,
        @Param("costAmount") BigDecimal costAmount
    );
}
