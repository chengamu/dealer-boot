package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.service.ReceiptService;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    private final ShipmentMapper shipmentMapper;
    private final SalesDocumentMapper documentMapper;
    private final FulfillmentAccessSupport access;
    private final ShipmentAggregationService aggregation;
    private final FulfillmentEventRecorder events;

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"@fulfillmentAccessSupport.shipment(#shipmentId).salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirm(Long shipmentId) {
        if (LoginHelper.isPlatformTenant()) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.merchantReceiptOnly");
        }
        return receive(shipmentId, null, false);
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"@fulfillmentAccessSupport.shipment(#shipmentId).salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean override(Long shipmentId, String reason) {
        access.platformOnly();
        if (StringUtils.isBlank(reason)) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.overrideReasonRequired");
        }
        return receive(shipmentId, reason, true);
    }

    private Boolean receive(Long shipmentId, String reason, boolean override) {
        Shipment shipment = access.shipment(shipmentId);
        if ("CONFIRMED".equals(shipment.getReceiptStatus())) return Boolean.TRUE;
        if ("DRAFT".equals(shipment.getStatus()) || "CANCELLED".equals(shipment.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.receiptDenied");
        }
        SalesDocument document = access.document(shipment.getSalesDocumentId());
        boolean updated = access.ignoreTenant(() -> shipmentMapper.update(null,
            new LambdaUpdateWrapper<Shipment>().eq(Shipment::getShipmentId, shipmentId)
                .eq(Shipment::getTenantId, shipment.getTenantId()).eq(Shipment::getDelFlag, "0")
                .eq(Shipment::getReceiptStatus, "PENDING").set(Shipment::getReceiptStatus, "CONFIRMED")
                .set(Shipment::getReceivedById, LoginHelper.getUserId()).set(Shipment::getReceivedBy, LoginHelper.getUsername())
                .set(Shipment::getReceivedTime, TimeUtils.utcNow()).set(override, Shipment::getReceiptOverrideReason, reason))) > 0;
        if (!updated) throw ServiceException.ofMessageKey("dealer.fulfillment.receiptDenied");
        events.record(document.getSalesDocumentId(), document.getTenantId(),
            override ? "PACKAGE_RECEIPT_OVERRIDDEN" : "PACKAGE_RECEIVED", "PENDING", "CONFIRMED", reason);
        completeOrder(document);
        return Boolean.TRUE;
    }

    private void completeOrder(SalesDocument document) {
        ShipmentAggregate aggregate = aggregation.aggregate(document);
        if (!aggregate.allReceived()) return;
        boolean completed = access.ignoreTenant(() -> documentMapper.update(null,
            new LambdaUpdateWrapper<SalesDocument>().eq(SalesDocument::getSalesDocumentId, document.getSalesDocumentId())
                .eq(SalesDocument::getTenantId, document.getTenantId()).eq(SalesDocument::getDelFlag, "0")
                .eq(SalesDocument::getDocumentStatus, "SUBMITTED")
                .set(SalesDocument::getShipmentStatus, "DELIVERED")
                .set(SalesDocument::getDocumentStatus, "COMPLETED")
                .set(SalesDocument::getDeliveredTime, TimeUtils.utcNow()))) > 0;
        if (completed) events.record(document.getSalesDocumentId(), document.getTenantId(),
            "ORDER_COMPLETED", "SUBMITTED", "COMPLETED", null);
    }
}
