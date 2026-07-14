package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.PayReconciliationCase;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ReconciliationCaseVo {
    Long caseId;
    String caseNo;
    String businessOrigin;
    Long tenantId;
    Long salesStoreId;
    Long payOrderId;
    Long extensionId;
    Long webhookEventId;
    Long salesDocumentId;
    String anomalyType;
    String severity;
    String status;
    LocalDateTime detectedTime;
    LocalDateTime lastCheckedTime;
    String diagnosisCode;
    String diagnosisMessage;
    String expectedSnapshotJson;
    String actualSnapshotJson;
    Long resolvedById;
    String resolvedBy;
    LocalDateTime resolvedTime;
    String resolutionCode;

    public static ReconciliationCaseVo from(PayReconciliationCase row) {
        if (row == null) return null;
        return builder().caseId(row.getCaseId()).caseNo(row.getCaseNo()).businessOrigin(row.getBusinessOrigin())
            .tenantId(row.getTenantId()).salesStoreId(row.getSalesStoreId()).payOrderId(row.getPayOrderId())
            .extensionId(row.getExtensionId()).webhookEventId(row.getWebhookEventId())
            .salesDocumentId(row.getSalesDocumentId()).anomalyType(row.getAnomalyType()).severity(row.getSeverity())
            .status(row.getStatus()).detectedTime(row.getDetectedTime()).lastCheckedTime(row.getLastCheckedTime())
            .diagnosisCode(row.getDiagnosisCode()).diagnosisMessage(row.getDiagnosisMessage())
            .expectedSnapshotJson(row.getExpectedSnapshotJson()).actualSnapshotJson(row.getActualSnapshotJson())
            .resolvedById(row.getResolvedById()).resolvedBy(row.getResolvedBy()).resolvedTime(row.getResolvedTime())
            .resolutionCode(row.getResolutionCode()).build();
    }
}
