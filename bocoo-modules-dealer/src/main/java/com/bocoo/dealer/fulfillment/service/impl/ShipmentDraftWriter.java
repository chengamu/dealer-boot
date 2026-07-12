package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentBo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentItemBo;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShipmentDraftWriter {
    private final ShipmentMapper shipmentMapper;
    private final ShipmentItemMapper itemMapper;
    private final FulfillmentAccessSupport access;

    public Shipment createHeader(SalesDocument document, ShipmentBo bo) {
        Shipment row = new Shipment();
        row.setTenantId(document.getTenantId());
        row.setMerchantId(document.getMerchantId());
        row.setMerchantName(document.getMerchantName());
        row.setSalesDocumentId(document.getSalesDocumentId());
        row.setShipmentNo("SHP-" + Seq.getId());
        row.setStatus("DRAFT");
        row.setReceiptStatus("PENDING");
        row.setDelFlag("0");
        copy(row, bo);
        access.ignoreTenant(() -> shipmentMapper.insert(row));
        return row;
    }

    public void updateHeader(Shipment row, ShipmentBo bo) {
        copy(row, bo);
        access.ignoreTenant(() -> shipmentMapper.updateById(row));
    }

    public void replaceItems(Shipment shipment, ShipmentBo bo, Map<Long, SalesDocumentItem> orderItems) {
        QueryWrapper<ShipmentItem> delete = new QueryWrapper<ShipmentItem>()
            .eq("tenant_id", shipment.getTenantId()).eq("shipment_id", shipment.getShipmentId());
        access.ignoreTenant(() -> itemMapper.delete(delete));
        int total = 0;
        for (ShipmentItemBo source : bo.getItems()) {
            SalesDocumentItem orderItem = orderItems.get(source.getSalesItemId());
            ShipmentItem row = new ShipmentItem();
            row.setTenantId(shipment.getTenantId());
            row.setShipmentId(shipment.getShipmentId());
            row.setSalesDocumentId(shipment.getSalesDocumentId());
            row.setSalesItemId(source.getSalesItemId());
            row.setLineNo(orderItem.getLineNo());
            row.setSaleProductCode(orderItem.getSaleProductCode());
            row.setSaleProductName(orderItem.getSaleProductName());
            row.setQuantity(source.getQuantity());
            row.setRemark(source.getRemark());
            access.ignoreTenant(() -> itemMapper.insert(row));
            total += source.getQuantity();
        }
        Shipment quantity = new Shipment();
        quantity.setShipmentId(shipment.getShipmentId());
        quantity.setItemQuantity(total);
        access.ignoreTenant(() -> shipmentMapper.updateById(quantity));
        shipment.setItemQuantity(total);
    }

    private void copy(Shipment row, ShipmentBo bo) {
        row.setPackageNo(bo.getPackageNo());
        row.setCarrierCode(bo.getCarrierCode());
        row.setCarrierName(bo.getCarrierName());
        row.setTrackingNo(bo.getTrackingNo());
        row.setWeight(bo.getWeight());
        row.setWeightUnit(bo.getWeightUnit());
        row.setLength(bo.getLength());
        row.setWidth(bo.getWidth());
        row.setHeight(bo.getHeight());
        row.setDimensionUnit(bo.getDimensionUnit());
        row.setLabelMediaId(bo.getLabelMediaId());
        row.setPackingListMediaId(bo.getPackingListMediaId());
        row.setRemark(bo.getRemark());
    }
}
