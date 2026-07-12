package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class FulfillmentAccessSupport {
    private final SalesDocumentMapper documentMapper;
    private final ShipmentMapper shipmentMapper;

    public SalesDocument document(Long id) {
        SalesDocument row = ignoreTenant(() -> documentMapper.selectOne(documentQuery(id), false));
        if (row == null) throw ServiceException.ofMessageKey("dealer.fulfillment.orderNotFound");
        return row;
    }

    public Shipment shipment(Long id) {
        Shipment row = ignoreTenant(() -> shipmentMapper.selectOne(shipmentQuery(id), false));
        if (row == null) throw ServiceException.ofMessageKey("dealer.fulfillment.shipmentNotFound");
        return row;
    }

    public void platformOnly() {
        if (!LoginHelper.isPlatformTenant()) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.platformOnly");
        }
    }

    public Long tenantId() {
        Long id = LoginHelper.getTenantId();
        if (id == null) throw ServiceException.ofMessageKey("tenant.context.missing");
        return id;
    }

    public <T> T ignoreTenant(Supplier<T> supplier) {
        return TenantContextHolder.callWithIgnore(supplier);
    }

    private QueryWrapper<SalesDocument> documentQuery(Long id) {
        QueryWrapper<SalesDocument> query = new QueryWrapper<SalesDocument>()
            .eq("sales_document_id", id).eq("del_flag", "0");
        if (!LoginHelper.isPlatformTenant()) query.eq("tenant_id", tenantId());
        return query;
    }

    private QueryWrapper<Shipment> shipmentQuery(Long id) {
        QueryWrapper<Shipment> query = new QueryWrapper<Shipment>()
            .eq("shipment_id", id).eq("del_flag", "0");
        if (!LoginHelper.isPlatformTenant()) query.eq("tenant_id", tenantId());
        return query;
    }
}
