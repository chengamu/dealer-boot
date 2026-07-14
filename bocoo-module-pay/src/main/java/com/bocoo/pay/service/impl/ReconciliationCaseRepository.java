package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayReconciliationCase;
import com.bocoo.pay.enums.ReconciliationCaseStatus;
import com.bocoo.pay.mapper.PayReconciliationCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
class ReconciliationCaseRepository {
    private final PayReconciliationCaseMapper mapper;

    PayReconciliationCase required(Long caseId) {
        PayReconciliationCase row = mapper.selectById(caseId);
        if (row == null) throw new ServiceException("Reconciliation case does not exist");
        return row;
    }

    PayReconciliationCase upsert(PayOrder order, PaymentDocumentFacts document, ReconciliationFinding finding) {
        PayReconciliationCase row = mapper.selectOne(new LambdaQueryWrapper<PayReconciliationCase>()
            .eq(PayReconciliationCase::getPayOrderId, order.getId())
            .eq(PayReconciliationCase::getAnomalyType, finding.type().name())
            .eq(PayReconciliationCase::getStatus, ReconciliationCaseStatus.OPEN.name()), false);
        if (row == null) return insert(order, document, finding);
        int rows = mapper.update(null, new LambdaUpdateWrapper<PayReconciliationCase>()
            .set(PayReconciliationCase::getLastCheckedTime, TimeUtils.utcNow())
            .set(PayReconciliationCase::getDiagnosisCode, finding.code())
            .set(PayReconciliationCase::getDiagnosisMessage, finding.message())
            .set(PayReconciliationCase::getExpectedSnapshotJson, finding.expectedJson())
            .set(PayReconciliationCase::getActualSnapshotJson, finding.actualJson())
            .setSql("version = version + 1")
            .eq(PayReconciliationCase::getCaseId, row.getCaseId())
            .eq(PayReconciliationCase::getStatus, ReconciliationCaseStatus.OPEN.name())
            .eq(PayReconciliationCase::getVersion, row.getVersion()));
        if (rows != 1) throw new ServiceException("Reconciliation case was changed concurrently");
        row.setLastCheckedTime(TimeUtils.utcNow());
        row.setDiagnosisCode(finding.code());
        row.setDiagnosisMessage(finding.message());
        row.setExpectedSnapshotJson(finding.expectedJson());
        row.setActualSnapshotJson(finding.actualJson());
        row.setVersion(row.getVersion() + 1);
        return row;
    }

    List<PayReconciliationCase> openByPayment(Long payOrderId) {
        return mapper.selectList(new LambdaQueryWrapper<PayReconciliationCase>()
            .eq(PayReconciliationCase::getPayOrderId, payOrderId)
            .eq(PayReconciliationCase::getStatus, ReconciliationCaseStatus.OPEN.name()));
    }

    void finish(PayReconciliationCase row, ReconciliationCaseStatus status, String code,
                Long userId, String username) {
        int rows = mapper.update(null, new LambdaUpdateWrapper<PayReconciliationCase>()
            .set(PayReconciliationCase::getStatus, status.name())
            .set(PayReconciliationCase::getResolvedById, userId)
            .set(PayReconciliationCase::getResolvedBy, username)
            .set(PayReconciliationCase::getResolvedTime, TimeUtils.utcNow())
            .set(PayReconciliationCase::getResolutionCode, code)
            .setSql("version = version + 1")
            .eq(PayReconciliationCase::getCaseId, row.getCaseId())
            .eq(PayReconciliationCase::getStatus, ReconciliationCaseStatus.OPEN.name())
            .eq(PayReconciliationCase::getVersion, row.getVersion()));
        if (rows != 1) throw new ServiceException("Reconciliation case was changed concurrently");
        row.setStatus(status.name());
        row.setResolutionCode(code);
    }

    private PayReconciliationCase insert(PayOrder order, PaymentDocumentFacts document,
                                         ReconciliationFinding finding) {
        PayReconciliationCase row = new PayReconciliationCase();
        row.setCaseNo("RC" + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + IdWorker.getId());
        row.setPayOrderId(order.getId());
        row.setSalesDocumentId(order.getSalesDocumentId());
        if (document != null) {
            row.setBusinessOrigin(document.getBusinessOrigin());
            row.setTenantId(document.getTenantId());
            row.setSalesStoreId(document.getSalesStoreId());
        } else {
            row.setTenantId(order.getPayerTenantId());
        }
        row.setAnomalyType(finding.type().name());
        row.setSeverity(finding.severity().name());
        row.setStatus(ReconciliationCaseStatus.OPEN.name());
        row.setDetectedTime(TimeUtils.utcNow());
        row.setLastCheckedTime(row.getDetectedTime());
        row.setDiagnosisCode(finding.code());
        row.setDiagnosisMessage(finding.message());
        row.setExpectedSnapshotJson(finding.expectedJson());
        row.setActualSnapshotJson(finding.actualJson());
        row.setVersion(0);
        mapper.insert(row);
        return row;
    }
}
