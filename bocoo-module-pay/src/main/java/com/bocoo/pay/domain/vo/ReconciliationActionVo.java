package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.PayReconciliationAction;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ReconciliationActionVo {
    Long actionId;
    String actionType;
    String beforeSnapshotJson;
    String afterSnapshotJson;
    String result;
    String resultCode;
    String resultMessage;
    Long operatorId;
    String operatorName;
    LocalDateTime occurredTime;
    String reason;

    public static ReconciliationActionVo from(PayReconciliationAction row) {
        return builder().actionId(row.getActionId()).actionType(row.getActionType())
            .beforeSnapshotJson(row.getBeforeSnapshotJson()).afterSnapshotJson(row.getAfterSnapshotJson())
            .result(row.getResult()).resultCode(row.getResultCode()).resultMessage(row.getResultMessage())
            .operatorId(row.getOperatorId()).operatorName(row.getOperatorName()).occurredTime(row.getOccurredTime())
            .reason(row.getReason()).build();
    }
}
