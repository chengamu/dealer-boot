package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentBo;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.service.ShipmentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentCommandServiceImpl implements ShipmentCommandService {
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper itemMapper;
    private final FulfillmentAccessSupport access;
    private final ShipmentAllocationValidator validator;
    private final ShipmentDraftWriter writer;
    private final ShipmentViewAssembler assembler;
    private final ShipmentDispatchSupport dispatchSupport;
    private final FulfillmentEventRecorder events;

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"#salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public ShipmentVo create(Long salesDocumentId, ShipmentBo bo) {
        SalesDocument document = access.document(salesDocumentId, FulfillmentAudience.FACTORY);
        requireProductionComplete(document);
        var orderItems = validator.validate(document, null, bo.getItems());
        Shipment shipment = writer.createHeader(document, bo);
        writer.replaceItems(shipment, bo, orderItems);
        events.record(salesDocumentId, document.getTenantId(), "SHIPMENT_DRAFT_CREATED", null, "DRAFT", shipment.getShipmentNo());
        return assembler.detail(shipment);
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"@fulfillmentAccessSupport.shipment(#shipmentId).salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(Long shipmentId, ShipmentBo bo) {
        Shipment shipment = access.shipment(shipmentId, FulfillmentAudience.FACTORY);
        if (!"DRAFT".equals(shipment.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.shipmentDraftOnly");
        }
        SalesDocument document = access.document(shipment.getSalesDocumentId(), FulfillmentAudience.FACTORY);
        var orderItems = validator.validate(document, shipmentId, bo.getItems());
        writer.updateHeader(shipment, bo);
        writer.replaceItems(shipment, bo, orderItems);
        events.record(document.getSalesDocumentId(), document.getTenantId(), "SHIPMENT_DRAFT_UPDATED", "DRAFT", "DRAFT", shipment.getShipmentNo());
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"@fulfillmentAccessSupport.shipment(#shipmentId).salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long shipmentId) {
        Shipment shipment = access.shipment(shipmentId, FulfillmentAudience.FACTORY);
        if (!"DRAFT".equals(shipment.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.shipmentDraftOnly");
        }
        Shipment deleted = new Shipment();
        deleted.setShipmentId(shipmentId);
        deleted.setDelFlag("2");
        access.ignoreTenant(() -> shipmentMapper.updateById(deleted));
        access.ignoreTenant(() -> itemMapper.delete(new QueryWrapper<ShipmentItem>()
            .eq("tenant_id", shipment.getTenantId()).eq("shipment_id", shipmentId)));
        events.record(shipment.getSalesDocumentId(), shipment.getTenantId(), "SHIPMENT_DRAFT_DELETED", "DRAFT", null, shipment.getShipmentNo());
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = "sales-document-lifecycle", keys = {"@fulfillmentAccessSupport.shipment(#shipmentId).salesDocumentId"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean dispatch(Long shipmentId) {
        return dispatchSupport.dispatch(shipmentId);
    }

    private void requireProductionComplete(SalesDocument document) {
        if (!"SUBMITTED".equals(document.getDocumentStatus()) || !"COMPLETED".equals(document.getProductionStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.productionNotComplete");
        }
    }

}
