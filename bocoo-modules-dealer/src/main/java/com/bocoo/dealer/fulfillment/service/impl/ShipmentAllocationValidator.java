package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentItemBo;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShipmentAllocationValidator {
    private final SalesDocumentItemMapper salesItemMapper;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper shipmentItemMapper;
    private final FulfillmentAccessSupport access;

    public Map<Long, SalesDocumentItem> validate(SalesDocument document, Long currentShipmentId,
                                                  List<ShipmentItemBo> requested) {
        Map<Long, SalesDocumentItem> orderItems = orderItems(document);
        Map<Long, Integer> allocated = allocated(document, currentShipmentId);
        HashSet<Long> seen = new HashSet<>();
        for (ShipmentItemBo item : requested) {
            SalesDocumentItem orderItem = orderItems.get(item.getSalesItemId());
            if (orderItem == null) throw ServiceException.ofMessageKey("dealer.fulfillment.foreignSalesItem");
            if (!seen.add(item.getSalesItemId())) {
                throw ServiceException.ofMessageKey("dealer.fulfillment.duplicateSalesItem");
            }
            int total = allocated.getOrDefault(item.getSalesItemId(), 0) + item.getQuantity();
            if (total > orderItem.getQuantity()) {
                throw ServiceException.ofMessageKey("dealer.fulfillment.quantityExceeded");
            }
        }
        return orderItems;
    }

    private Map<Long, SalesDocumentItem> orderItems(SalesDocument document) {
        QueryWrapper<SalesDocumentItem> query = new QueryWrapper<SalesDocumentItem>()
            .eq("tenant_id", document.getTenantId()).eq("sales_document_id", document.getSalesDocumentId())
            .eq("del_flag", "0");
        Map<Long, SalesDocumentItem> result = new HashMap<>();
        access.ignoreTenant(() -> salesItemMapper.selectList(query))
            .forEach(item -> result.put(item.getSalesItemId(), item));
        return result;
    }

    private Map<Long, Integer> allocated(SalesDocument document, Long currentShipmentId) {
        QueryWrapper<Shipment> shipments = new QueryWrapper<Shipment>().select("shipment_id")
            .eq("tenant_id", document.getTenantId()).eq("sales_document_id", document.getSalesDocumentId())
            .eq("del_flag", "0").ne("status", "CANCELLED")
            .ne(currentShipmentId != null, "shipment_id", currentShipmentId);
        List<Long> ids = access.ignoreTenant(() -> shipmentMapper.selectList(shipments)).stream()
            .map(Shipment::getShipmentId).toList();
        if (ids.isEmpty()) return new HashMap<>();
        QueryWrapper<ShipmentItem> items = new QueryWrapper<ShipmentItem>()
            .eq("tenant_id", document.getTenantId()).in("shipment_id", ids);
        Map<Long, Integer> result = new HashMap<>();
        access.ignoreTenant(() -> shipmentItemMapper.selectList(items)).forEach(item ->
            result.merge(item.getSalesItemId(), item.getQuantity(), Integer::sum));
        return result;
    }
}
