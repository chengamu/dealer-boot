package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentItemBo;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ShipmentDispatchSupport {
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper itemMapper;
    private final SalesDocumentMapper documentMapper;
    private final FulfillmentAccessSupport access;
    private final ShipmentAllocationValidator validator;
    private final ShipmentAggregationService aggregation;
    private final FulfillmentEventRecorder events;

    public Boolean dispatch(Long shipmentId) {
        Shipment shipment = access.shipment(shipmentId);
        SalesDocument document = access.document(shipment.getSalesDocumentId());
        requireDispatchable(shipment, document);
        validator.validate(document, shipmentId, shipmentItems(shipment));
        checkTrackingNumber(shipment);
        boolean updated = access.ignoreTenant(() -> shipmentMapper.update(null,
            new LambdaUpdateWrapper<Shipment>().eq(Shipment::getShipmentId, shipmentId)
                .eq(Shipment::getTenantId, shipment.getTenantId()).eq(Shipment::getStatus, "DRAFT")
                .eq(Shipment::getDelFlag, "0").set(Shipment::getStatus, "DISPATCHED")
                .set(Shipment::getShippedById, LoginHelper.getUserId()).set(Shipment::getShippedBy, LoginHelper.getUsername())
                .set(Shipment::getShippedTime, TimeUtils.utcNow()))) > 0;
        if (!updated) throw ServiceException.ofMessageKey("dealer.fulfillment.dispatchDenied");
        ShipmentAggregate aggregate = aggregation.aggregate(document);
        updateDocument(document, shipment, aggregate);
        events.record(document.getSalesDocumentId(), document.getTenantId(), "SHIPMENT_DISPATCHED",
            document.getShipmentStatus(), aggregate.shipmentStatus(), shipment.getShipmentNo());
        return Boolean.TRUE;
    }

    private void requireDispatchable(Shipment shipment, SalesDocument document) {
        boolean invalidOrder = !"SUBMITTED".equals(document.getDocumentStatus())
            || !"COMPLETED".equals(document.getProductionStatus());
        boolean invalidShipment = !"DRAFT".equals(shipment.getStatus())
            || StringUtils.isBlank(shipment.getCarrierCode()) || StringUtils.isBlank(shipment.getCarrierName())
            || StringUtils.isBlank(shipment.getTrackingNo()) || shipment.getItemQuantity() == null
            || shipment.getItemQuantity() <= 0;
        if (invalidOrder || invalidShipment) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.dispatchDenied");
        }
    }

    private List<ShipmentItemBo> shipmentItems(Shipment shipment) {
        List<ShipmentItem> rows = access.ignoreTenant(() -> itemMapper.selectList(new QueryWrapper<ShipmentItem>()
            .eq("tenant_id", shipment.getTenantId()).eq("shipment_id", shipment.getShipmentId())));
        return rows.stream().map(item -> {
            ShipmentItemBo bo = new ShipmentItemBo();
            bo.setSalesItemId(item.getSalesItemId());
            bo.setQuantity(item.getQuantity());
            return bo;
        }).toList();
    }

    private void checkTrackingNumber(Shipment shipment) {
        Long count = access.ignoreTenant(() -> shipmentMapper.selectCount(new QueryWrapper<Shipment>()
            .eq("carrier_code", shipment.getCarrierCode()).eq("tracking_no", shipment.getTrackingNo())
            .eq("del_flag", "0").notIn("status", "DRAFT", "CANCELLED")
            .ne("shipment_id", shipment.getShipmentId())));
        if (count > 0) throw ServiceException.ofMessageKey("dealer.fulfillment.trackingNumberExists");
    }

    private void updateDocument(SalesDocument document, Shipment shipment, ShipmentAggregate aggregate) {
        var shippedTime = TimeUtils.utcNow();
        LambdaUpdateWrapper<SalesDocument> update = new LambdaUpdateWrapper<SalesDocument>()
            .eq(SalesDocument::getSalesDocumentId, document.getSalesDocumentId())
            .eq(SalesDocument::getTenantId, document.getTenantId()).eq(SalesDocument::getDelFlag, "0")
            .set(SalesDocument::getShipmentStatus, aggregate.shipmentStatus())
            .set(SalesDocument::getShipmentCount, aggregate.packageCount())
            .set(SalesDocument::getShippedQuantity, aggregate.dispatchedQuantity())
            .set(SalesDocument::getLatestShippedTime, shippedTime)
            .set(SalesDocument::getCarrierName, shipment.getCarrierName())
            .set(SalesDocument::getTrackingNo, shipment.getTrackingNo());
        if (document.getShippedTime() == null) {
            update.set(SalesDocument::getShippedTime, shippedTime)
                .set(SalesDocument::getFirstShippedTime, shippedTime);
        }
        access.ignoreTenant(() -> documentMapper.update(null, update));
    }
}
