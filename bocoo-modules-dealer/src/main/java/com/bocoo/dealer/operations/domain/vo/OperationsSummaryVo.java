package com.bocoo.dealer.operations.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class OperationsSummaryVo {

    private long pendingApplicationCount;
    private long enabledMerchantCount;
    private long disabledMerchantCount;
    private long vipMerchantCount;
    private OperationsOrderLifecycleVo orderLifecycle;
    private Map<String, BigDecimal> currencyAmounts;
    private LocalDateTime dataAsOf;
}
