package com.bocoo.dealer.operations.domain.vo;

import lombok.Data;

@Data
public class OperationsOrderLifecycleVo {

    private long submittedCount;
    private long unpaidCount;
    private long productionCount;
    private long shippedCount;
    private long completedCount;
}
