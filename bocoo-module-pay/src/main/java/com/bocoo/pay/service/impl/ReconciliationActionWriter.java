package com.bocoo.pay.service.impl;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.entity.PayReconciliationAction;
import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.enums.ReconciliationActionType;
import com.bocoo.pay.mapper.PayReconciliationActionMapper;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ReconciliationActionWriter {
    private final PayReconciliationActionMapper mapper;
    private final PayOperatorContext operator;

    void write(PayReconciliationCase row, ReconciliationActionType type, String before, String after,
               String result, String code, String message, String reason) {
        PayReconciliationAction action = new PayReconciliationAction();
        action.setCaseId(row.getCaseId());
        action.setActionType(type.name());
        action.setBeforeSnapshotJson(before);
        action.setAfterSnapshotJson(after);
        action.setResult(result);
        action.setResultCode(code);
        action.setResultMessage(message);
        action.setOperatorId(operator.userId());
        action.setOperatorName(operator.username());
        action.setOccurredTime(TimeUtils.utcNow());
        action.setReason(reason);
        mapper.insert(action);
    }
}
