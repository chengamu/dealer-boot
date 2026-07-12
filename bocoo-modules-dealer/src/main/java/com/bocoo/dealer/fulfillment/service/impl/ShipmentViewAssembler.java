package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.domain.entity.TrackingEvent;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.mapper.TrackingEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ShipmentViewAssembler {
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper itemMapper;
    private final TrackingEventMapper eventMapper;
    private final FulfillmentAccessSupport access;

    public List<ShipmentVo> byDocument(Long documentId, Long tenantId) {
        QueryWrapper<Shipment> query = new QueryWrapper<Shipment>().eq("tenant_id", tenantId)
            .eq("sales_document_id", documentId).eq("del_flag", "0")
            .orderByDesc("create_time", "shipment_id");
        List<ShipmentVo> rows = access.ignoreTenant(() -> shipmentMapper.selectVoList(query));
        rows.forEach(this::fill);
        return rows;
    }

    public ShipmentVo detail(Shipment shipment) {
        ShipmentVo vo = access.ignoreTenant(() -> shipmentMapper.selectVoById(shipment.getShipmentId()));
        fill(vo);
        return vo;
    }

    public void fill(ShipmentVo vo) {
        QueryWrapper<ShipmentItem> items = new QueryWrapper<ShipmentItem>()
            .eq("tenant_id", vo.getTenantId()).eq("shipment_id", vo.getShipmentId())
            .orderByAsc("line_no", "shipment_item_id");
        QueryWrapper<TrackingEvent> events = new QueryWrapper<TrackingEvent>()
            .eq("tenant_id", vo.getTenantId()).eq("shipment_id", vo.getShipmentId())
            .orderByDesc("occurred_time", "tracking_event_id");
        vo.setItems(access.ignoreTenant(() -> itemMapper.selectVoList(items)));
        vo.setTrackingEvents(access.ignoreTenant(() -> eventMapper.selectVoList(events)));
    }
}
