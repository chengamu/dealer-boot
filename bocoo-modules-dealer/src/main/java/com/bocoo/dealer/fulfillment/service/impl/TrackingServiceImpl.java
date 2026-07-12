package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.TrackingEvent;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.mapper.TrackingEventMapper;
import com.bocoo.dealer.fulfillment.service.TrackingService;
import com.bocoo.dealer.fulfillment.tracking.TrackingProvider;
import com.bocoo.dealer.fulfillment.tracking.TrackingSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {
    private final List<TrackingProvider> providers;
    private final ShipmentMapper shipmentMapper;
    private final TrackingEventMapper eventMapper;
    private final TrackingEventWriter writer;
    private final FulfillmentAccessSupport access;

    @Override
    @Lock4j(name = "shipment-tracking-sync", keys = {"#shipmentId"})
    public List<TrackingEventVo> sync(Long shipmentId) {
        access.platformOnly();
        Shipment shipment = access.shipment(shipmentId);
        if ("DRAFT".equals(shipment.getStatus()) || "CANCELLED".equals(shipment.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.trackingUnavailable");
        }
        TrackingProvider provider = provider(shipment.getCarrierCode());
        try {
            TrackingSnapshot snapshot = provider.fetch(shipment);
            LocalDateTime latest = writer.insertNew(shipment, snapshot.events());
            applySnapshot(shipment, snapshot, latest);
        } catch (RuntimeException ex) {
            recordFailure(shipment, ex);
            throw ServiceException.ofMessageKey("dealer.fulfillment.trackingSyncFailed");
        }
        return events(shipmentId);
    }

    @Override
    public List<TrackingEventVo> events(Long shipmentId) {
        Shipment shipment = access.shipment(shipmentId);
        QueryWrapper<TrackingEvent> query = new QueryWrapper<TrackingEvent>()
            .eq("tenant_id", shipment.getTenantId()).eq("shipment_id", shipmentId)
            .orderByDesc("occurred_time", "tracking_event_id");
        return access.ignoreTenant(() -> eventMapper.selectVoList(query));
    }

    private TrackingProvider provider(String carrierCode) {
        return providers.stream().filter(item -> item.supports(carrierCode)).findFirst()
            .orElseThrow(() -> ServiceException.ofMessageKey("dealer.fulfillment.trackingProviderMissing"));
    }

    private void applySnapshot(Shipment shipment, TrackingSnapshot snapshot, LocalDateTime latest) {
        LambdaUpdateWrapper<Shipment> update = new LambdaUpdateWrapper<Shipment>()
            .eq(Shipment::getShipmentId, shipment.getShipmentId()).eq(Shipment::getTenantId, shipment.getTenantId())
            .eq(Shipment::getDelFlag, "0").set(Shipment::getTrackingStatus, snapshot.status())
            .set(Shipment::getLastTrackingTime, latest).set(Shipment::getTrackingErrorCode, null)
            .set(Shipment::getTrackingErrorMessage, null);
        if ("IN_TRANSIT".equals(snapshot.status()) || "CARRIER_DELIVERED".equals(snapshot.status())
            || "EXCEPTION".equals(snapshot.status())) {
            update.set(Shipment::getStatus, snapshot.status());
        }
        access.ignoreTenant(() -> shipmentMapper.update(null, update));
    }

    private void recordFailure(Shipment shipment, RuntimeException ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        if (message.length() > 1000) message = message.substring(0, 1000);
        String finalMessage = message;
        access.ignoreTenant(() -> shipmentMapper.update(null, new LambdaUpdateWrapper<Shipment>()
            .eq(Shipment::getShipmentId, shipment.getShipmentId()).eq(Shipment::getTenantId, shipment.getTenantId())
            .eq(Shipment::getDelFlag, "0").set(Shipment::getTrackingErrorCode, "SYNC_FAILED")
            .set(Shipment::getTrackingErrorMessage, finalMessage)));
    }
}
