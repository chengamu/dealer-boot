package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.bo.SalesPaymentBo;
import com.bocoo.dealer.domain.bo.SalesShipmentBo;
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

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmPayment(Long id, SalesPaymentBo bo) {
        platformOnly();
        SalesDocument row = accessible(id);
        requireStatus(row, "SUBMITTED");
        if ("PAID".equals(row.getPaymentStatus())) return Boolean.TRUE;
        if (bo.getPaidAmount().compareTo(row.getTotalAmount()) != 0) {
            throw ServiceException.ofMessageKey("dealer.sales.paymentAmountMismatch");
        }
        return commit(row, "PAYMENT_CONFIRMED", "UNPAID", "PAID", bo.getNote(), baseUpdate(row)
            .eq(SalesDocument::getDocumentStatus, "SUBMITTED")
            .eq(SalesDocument::getPaymentStatus, "UNPAID")
            .set(SalesDocument::getPaymentStatus, "PAID")
            .set(SalesDocument::getPaymentMethod, bo.getPaymentMethod())
            .set(SalesDocument::getPaidAmount, bo.getPaidAmount())
            .set(SalesDocument::getPaymentReference, bo.getPaymentReference())
            .set(SalesDocument::getPaymentProofMediaId, bo.getPaymentProofMediaId())
            .set(SalesDocument::getPaidTime, TimeUtils.utcNow())
            .set(SalesDocument::getPaymentConfirmedById, LoginHelper.getUserId())
            .set(SalesDocument::getPaymentConfirmedBy, LoginHelper.getUsername()), "dealer.sales.statusDenied");
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean startProduction(Long id) {
        platformOnly();
        SalesDocument row = accessible(id);
        requireStatus(row, "SUBMITTED");
        if (!"PAID".equals(row.getPaymentStatus()) || !"PENDING".equals(row.getProductionStatus())) {
            throw ServiceException.ofMessageKey("dealer.sales.productionStartDenied");
        }
        return commit(row, "PRODUCTION_STARTED", "PENDING", "IN_PRODUCTION", null, baseUpdate(row)
            .eq(SalesDocument::getDocumentStatus, "SUBMITTED")
            .eq(SalesDocument::getPaymentStatus, "PAID")
            .eq(SalesDocument::getProductionStatus, "PENDING")
            .set(SalesDocument::getProductionStatus, "IN_PRODUCTION")
            .set(SalesDocument::getProductionStartTime, TimeUtils.utcNow()), "dealer.sales.productionStartDenied");
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeProduction(Long id) {
        platformOnly();
        SalesDocument row = accessible(id);
        if (!"IN_PRODUCTION".equals(row.getProductionStatus())) {
            throw ServiceException.ofMessageKey("dealer.sales.productionCompleteDenied");
        }
        return commit(row, "PRODUCTION_COMPLETED", "IN_PRODUCTION", "COMPLETED", null, baseUpdate(row)
            .eq(SalesDocument::getProductionStatus, "IN_PRODUCTION")
            .set(SalesDocument::getProductionStatus, "COMPLETED")
            .set(SalesDocument::getProductionCompleteTime, TimeUtils.utcNow()), "dealer.sales.productionCompleteDenied");
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean ship(Long id, SalesShipmentBo bo) {
        platformOnly();
        SalesDocument row = accessible(id);
        if (!"COMPLETED".equals(row.getProductionStatus()) || !"UNSHIPPED".equals(row.getShipmentStatus())) {
            throw ServiceException.ofMessageKey("dealer.sales.shipDenied");
        }
        return commit(row, "SHIPPED", "UNSHIPPED", "SHIPPED", bo.getNote(), baseUpdate(row)
            .eq(SalesDocument::getProductionStatus, "COMPLETED")
            .eq(SalesDocument::getShipmentStatus, "UNSHIPPED")
            .set(SalesDocument::getShipmentStatus, "SHIPPED")
            .set(SalesDocument::getCarrierName, bo.getCarrierName())
            .set(SalesDocument::getTrackingNo, bo.getTrackingNo())
            .set(SalesDocument::getShippedTime, TimeUtils.utcNow()), "dealer.sales.shipDenied");
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean deliver(Long id) {
        SalesDocument row = merchantDocument(id);
        if (!"SHIPPED".equals(row.getShipmentStatus())) throw ServiceException.ofMessageKey("dealer.sales.deliverDenied");
        return commit(row, "DELIVERED", "SHIPPED", "COMPLETED", null, baseUpdate(row)
            .eq(SalesDocument::getShipmentStatus, "SHIPPED")
            .set(SalesDocument::getShipmentStatus, "DELIVERED")
            .set(SalesDocument::getDeliveredTime, TimeUtils.utcNow())
            .set(SalesDocument::getDocumentStatus, "COMPLETED"), "dealer.sales.deliverDenied");
    }

    private SalesDocument merchantDocument(Long id) {
        SalesDocument row = mapper.selectOne(this.<SalesDocument>active().eq("tenant_id", tenantId()).eq("sales_document_id", id), false);
        if (row == null) throw ServiceException.ofMessageKey("dealer.sales.notFound");
        return row;
    }

    private SalesDocument accessible(Long id) {
        SalesDocument row = ignoreTenant(() -> mapper.selectOne(this.<SalesDocument>active().eq("sales_document_id", id), false));
        if (row == null || (!LoginHelper.isPlatformTenant() && !tenantId().equals(row.getTenantId()))) {
            throw ServiceException.ofMessageKey("dealer.sales.notFound");
        }
        return row;
    }

    private void requireStatus(SalesDocument row, String status) {
        if (!status.equals(row.getDocumentStatus())) throw ServiceException.ofMessageKey("dealer.sales.statusDenied");
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

    private void platformOnly() {
        if (!LoginHelper.isPlatformTenant()) throw ServiceException.ofMessageKey("dealer.sales.platformOnly");
    }
}
