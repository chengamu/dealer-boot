package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShipmentAggregationService {
    private final SalesDocumentItemMapper salesItemMapper;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper shipmentItemMapper;
    private final FulfillmentAccessSupport access;

    public ShipmentAggregate aggregate(SalesDocument document) {
        List<SalesDocumentItem> orderItems = orderItems(document);
        List<Shipment> shipments = dispatchedShipments(document);
        Map<Long, Integer> dispatched = dispatchedQuantities(document, shipments);
        int total = orderItems.stream().mapToInt(item -> value(item.getQuantity())).sum();
        int sent = dispatched.values().stream().mapToInt(Integer::intValue).sum();
        boolean complete = !orderItems.isEmpty() && orderItems.stream()
            .allMatch(item -> dispatched.getOrDefault(item.getSalesItemId(), 0) == value(item.getQuantity()));
        boolean received = complete && !shipments.isEmpty() && shipments.stream()
            .allMatch(item -> "CONFIRMED".equals(item.getReceiptStatus()));
        String status = received ? "DELIVERED" : complete ? "SHIPPED" : sent > 0 ? "PARTIALLY_SHIPPED" : "UNSHIPPED";
        return new ShipmentAggregate(status, total, sent, shipments.size(), received);
    }

    private List<SalesDocumentItem> orderItems(SalesDocument document) {
        QueryWrapper<SalesDocumentItem> query = new QueryWrapper<SalesDocumentItem>()
            .eq("tenant_id", document.getTenantId()).eq("sales_document_id", document.getSalesDocumentId())
            .eq("del_flag", "0");
        return access.ignoreTenant(() -> salesItemMapper.selectList(query));
    }

    private List<Shipment> dispatchedShipments(SalesDocument document) {
        QueryWrapper<Shipment> query = new QueryWrapper<Shipment>()
            .eq("tenant_id", document.getTenantId()).eq("sales_document_id", document.getSalesDocumentId())
            .eq("del_flag", "0").notIn("status", "DRAFT", "CANCELLED");
        return access.ignoreTenant(() -> shipmentMapper.selectList(query));
    }

    private Map<Long, Integer> dispatchedQuantities(SalesDocument document, List<Shipment> shipments) {
        if (shipments.isEmpty()) return Map.of();
        List<Long> ids = shipments.stream().map(Shipment::getShipmentId).toList();
        QueryWrapper<ShipmentItem> query = new QueryWrapper<ShipmentItem>()
            .eq("tenant_id", document.getTenantId()).in("shipment_id", ids);
        Map<Long, Integer> result = new HashMap<>();
        access.ignoreTenant(() -> shipmentItemMapper.selectList(query)).forEach(item ->
            result.merge(item.getSalesItemId(), item.getQuantity(), Integer::sum));
        return result;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
