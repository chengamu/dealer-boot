package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.TrackingEvent;
import com.bocoo.dealer.fulfillment.mapper.TrackingEventMapper;
import com.bocoo.dealer.fulfillment.tracking.TrackingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrackingEventWriter {
    private final TrackingEventMapper mapper;
    private final FulfillmentAccessSupport access;

    public LocalDateTime insertNew(Shipment shipment, List<TrackingRecord> records) {
        LocalDateTime latest = shipment.getLastTrackingTime();
        for (TrackingRecord record : records) {
            if (exists(shipment, record)) continue;
            access.ignoreTenant(() -> mapper.insert(entity(shipment, record)));
            if (latest == null || record.occurredTime().isAfter(latest)) latest = record.occurredTime();
        }
        return latest;
    }

    private boolean exists(Shipment shipment, TrackingRecord record) {
        QueryWrapper<TrackingEvent> query = new QueryWrapper<TrackingEvent>()
            .eq("shipment_id", shipment.getShipmentId())
            .eq("carrier_code", shipment.getCarrierCode());
        if (record.providerEventId() != null) {
            query.eq("provider_event_id", record.providerEventId());
        } else {
            query.isNull("provider_event_id").eq("tracking_no", shipment.getTrackingNo())
                .eq("event_code", record.eventCode()).eq("occurred_time", record.occurredTime());
        }
        return access.ignoreTenant(() -> mapper.selectCount(query)) > 0;
    }

    private TrackingEvent entity(Shipment shipment, TrackingRecord source) {
        TrackingEvent row = new TrackingEvent();
        row.setTenantId(shipment.getTenantId());
        row.setShipmentId(shipment.getShipmentId());
        row.setCarrierCode(shipment.getCarrierCode());
        row.setTrackingNo(shipment.getTrackingNo());
        row.setProviderEventId(source.providerEventId());
        row.setEventCode(source.eventCode());
        row.setEventStatus(source.eventStatus());
        row.setDescriptionOriginal(source.descriptionOriginal());
        row.setDescriptionCn(source.descriptionCn());
        row.setDescriptionEn(source.descriptionEn());
        row.setLocation(source.location());
        row.setOccurredTime(source.occurredTime());
        row.setSource("API");
        row.setReceivedTime(TimeUtils.utcNow());
        row.setRawDataRef(source.rawDataRef());
        return row;
    }
}
