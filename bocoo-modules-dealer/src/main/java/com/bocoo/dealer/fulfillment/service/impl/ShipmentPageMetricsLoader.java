package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.vo.SalesDocumentItemMetricsVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentPackageMetricsVo;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class ShipmentPageMetricsLoader {
    private final SalesDocumentItemMapper salesItemMapper;
    private final ShipmentMapper shipmentMapper;
    private final FulfillmentAccessSupport access;

    Map<DocumentKey, ShipmentPageMetrics> load(List<SalesDocument> documents) {
        if (documents.isEmpty()) return Map.of();
        List<Long> tenantIds = documents.stream().map(SalesDocument::getTenantId).distinct().toList();
        List<Long> documentIds = documents.stream().map(SalesDocument::getSalesDocumentId).toList();
        Map<DocumentKey, SalesDocumentItemMetricsVo> sales = access.ignoreTenant(
            () -> salesItemMapper.selectPageMetrics(tenantIds, documentIds)).stream()
            .collect(Collectors.toMap(item -> new DocumentKey(item.getTenantId(), item.getSalesDocumentId()), item -> item));
        Map<DocumentKey, ShipmentPackageMetricsVo> packages = access.ignoreTenant(
            () -> shipmentMapper.selectPackageMetrics(tenantIds, documentIds)).stream()
            .collect(Collectors.toMap(item -> new DocumentKey(item.getTenantId(), item.getSalesDocumentId()), item -> item));
        Map<DocumentKey, ShipmentPageMetrics> result = new HashMap<>();
        documents.forEach(document -> {
            DocumentKey key = new DocumentKey(document.getTenantId(), document.getSalesDocumentId());
            SalesDocumentItemMetricsVo item = sales.get(key);
            ShipmentPackageMetricsVo pack = packages.get(key);
            result.put(key, metrics(item, pack));
        });
        return result;
    }

    private ShipmentPageMetrics metrics(SalesDocumentItemMetricsVo item, ShipmentPackageMetricsVo pack) {
        int itemCount = item == null ? 0 : value(item.getItemCount());
        int total = item == null ? 0 : value(item.getTotalQuantity());
        int sent = item == null ? 0 : value(item.getDispatchedQuantity());
        int packageCount = pack == null ? 0 : value(pack.getPackageCount());
        int receivedCount = pack == null ? 0 : value(pack.getReceivedPackageCount());
        boolean complete = itemCount > 0 && Boolean.TRUE.equals(item.getAllDispatched());
        boolean received = complete && packageCount > 0 && packageCount == receivedCount;
        String status = received ? "DELIVERED" : complete ? "SHIPPED"
            : sent > 0 ? "PARTIALLY_SHIPPED" : "UNSHIPPED";
        return new ShipmentPageMetrics(itemCount,
            new ShipmentAggregate(status, total, sent, packageCount, received));
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    record DocumentKey(Long tenantId, Long documentId) {
    }
}
