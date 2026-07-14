package com.bocoo.pay.domain.vo;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ReconciliationCaseDetailVo {
    ReconciliationCaseVo reconciliationCase;
    PayOrderDetailVo payment;
    List<ReconciliationActionVo> actions;
}
