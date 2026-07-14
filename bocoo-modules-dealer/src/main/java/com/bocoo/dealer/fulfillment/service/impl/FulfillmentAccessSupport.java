package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FulfillmentAccessSupport {
    private final SalesDocumentMapper documentMapper;
    private final ShipmentMapper shipmentMapper;
    private final FulfillmentBusinessScopeResolver businessScopeResolver;

    public SalesDocument document(Long id, FulfillmentAudience audience) {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>()
            .eq("sales_document_id", id).eq("del_flag", "0");
        applyScope(query, audience);
        SalesDocument row = ignoreTenant(() -> documentMapper.selectOne(query, false));
        if (row == null) throw ServiceException.ofMessageKey("dealer.fulfillment.orderNotFound");
        return row;
    }

    public Shipment shipment(Long id) {
        return shipment(id, FulfillmentAudience.FACTORY);
    }

    public Shipment shipment(Long id, FulfillmentAudience audience) {
        Shipment row = ignoreTenant(() -> shipmentMapper.selectOne(new QueryWrapper<Shipment>()
            .eq("shipment_id", id).eq("del_flag", "0"), false));
        if (row == null) throw ServiceException.ofMessageKey("dealer.fulfillment.shipmentNotFound");
        if (audience == FulfillmentAudience.BUSINESS) {
            document(row.getSalesDocumentId(), audience);
        }
        return row;
    }

    public List<Shipment> shipments(List<Long> ids, FulfillmentAudience audience) {
        List<Long> distinctIds = ids == null ? List.of() : ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) return List.of();
        List<Shipment> rows = ignoreTenant(() -> shipmentMapper.selectList(new QueryWrapper<Shipment>()
            .in("shipment_id", distinctIds).eq("del_flag", "0")));
        if (rows.size() != distinctIds.size()) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.shipmentNotFound");
        }
        if (audience == FulfillmentAudience.BUSINESS) {
            List<Long> documentIds = rows.stream().map(Shipment::getSalesDocumentId).distinct().toList();
            QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>()
                .in("sales_document_id", documentIds).eq("del_flag", "0");
            applyScope(query, audience);
            List<SalesDocument> documents = ignoreTenant(() -> documentMapper.selectList(query));
            if (documents.size() != documentIds.size()) {
                throw ServiceException.ofMessageKey("dealer.fulfillment.shipmentNotFound");
            }
        }
        return rows;
    }

    public <T> T ignoreTenant(Supplier<T> supplier) {
        return TenantContextHolder.callWithIgnore(supplier);
    }

    public void applyScope(QueryWrapper<SalesDocument> query, FulfillmentAudience audience) {
        if (audience == FulfillmentAudience.BUSINESS) {
            businessScopeResolver.resolve().apply(query);
        }
    }
}
