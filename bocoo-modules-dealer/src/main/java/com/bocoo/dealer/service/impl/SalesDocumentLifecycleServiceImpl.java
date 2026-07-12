package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.SalesDocumentLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesDocumentLifecycleServiceImpl extends DealerServiceSupport implements SalesDocumentLifecycleService {
    private final SalesDocumentMapper mapper;
    private final SalesDocumentEventRecorder events;

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(Long id, String reason) {
        SalesDocument row = accessible(id);
        if ("COMPLETED".equals(row.getDocumentStatus()) || "CANCELLED".equals(row.getDocumentStatus())
            || "PAID".equals(row.getPaymentStatus()) || !"PENDING".equals(row.getProductionStatus())) {
            throw ServiceException.ofMessageKey("dealer.sales.cancelDenied");
        }
        return commit(row, "CANCEL", row.getDocumentStatus(), "CANCELLED", reason, baseUpdate(row)
            .eq(SalesDocument::getDocumentStatus, row.getDocumentStatus())
            .eq(SalesDocument::getPaymentStatus, row.getPaymentStatus())
            .eq(SalesDocument::getProductionStatus, row.getProductionStatus())
            .set(SalesDocument::getDocumentStatus, "CANCELLED"), "dealer.sales.cancelDenied");
    }

    private SalesDocument accessible(Long id) {
        SalesDocument row = ignoreTenant(() -> mapper.selectOne(this.<SalesDocument>active().eq("sales_document_id", id), false));
        if (row == null || (!LoginHelper.isPlatformTenant() && !tenantId().equals(row.getTenantId()))) {
            throw ServiceException.ofMessageKey("dealer.sales.notFound");
        }
        return row;
    }

    private LambdaUpdateWrapper<SalesDocument> baseUpdate(SalesDocument row) {
        return new LambdaUpdateWrapper<SalesDocument>()
            .eq(SalesDocument::getSalesDocumentId, row.getSalesDocumentId())
            .eq(SalesDocument::getTenantId, row.getTenantId())
            .eq(SalesDocument::getDelFlag, "0");
    }

    private Boolean commit(SalesDocument row, String event, String from, String to, String note,
                           LambdaUpdateWrapper<SalesDocument> update, String failureKey) {
        boolean updated = ignoreTenant(() -> mapper.update(null, update)) > 0;
        if (!updated) throw ServiceException.ofMessageKey(failureKey);
        events.record(row.getSalesDocumentId(), row.getTenantId(), event, from, to, note);
        return Boolean.TRUE;
    }
}
