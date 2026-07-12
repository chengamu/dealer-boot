package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentItemVo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FulfillmentOrderAssembler {
    private final SalesDocumentItemMapper itemMapper;
    private final SalesDocumentEventMapper eventMapper;
    private final ShipmentViewAssembler shipmentAssembler;
    private final FulfillmentAccessSupport access;

    public ProductionOrderVo summary(SalesDocument row) {
        List<SalesDocumentItem> items = items(row);
        return summary(row, items.size(), items.stream().mapToInt(item -> value(item.getQuantity())).sum());
    }

    public ProductionOrderVo summary(SalesDocument row, int itemCount, int totalQuantity) {
        ProductionOrderVo vo = new ProductionOrderVo();
        copySummary(row, vo);
        vo.setItemCount(itemCount);
        vo.setTotalQuantity(totalQuantity);
        return vo;
    }

    public FulfillmentOrderVo detail(SalesDocument row) {
        FulfillmentOrderVo vo = new FulfillmentOrderVo();
        copySummary(row, vo);
        vo.setDocumentStatus(row.getDocumentStatus());
        vo.setPaymentStatus(row.getPaymentStatus());
        vo.setShipmentStatus(row.getShipmentStatus());
        vo.setRecipientName(row.getRecipientName());
        vo.setRecipientPhone(row.getRecipientPhone());
        vo.setShippingAddress(row.getShippingAddress());
        vo.setDeliveredTime(row.getDeliveredTime());
        List<SalesDocumentItem> items = items(row);
        vo.setItemCount(items.size());
        vo.setTotalQuantity(items.stream().mapToInt(item -> value(item.getQuantity())).sum());
        vo.setItems(items.stream().map(item -> MapstructUtils.convert(item, FulfillmentItemVo.class)).toList());
        vo.setShipments(shipmentAssembler.byDocument(row.getSalesDocumentId(), row.getTenantId()));
        vo.setEvents(access.ignoreTenant(() -> eventMapper.selectVoList(
            new QueryWrapper<com.bocoo.dealer.domain.entity.SalesDocumentEvent>()
                .eq("tenant_id", row.getTenantId()).eq("sales_document_id", row.getSalesDocumentId())
                .orderByDesc("occurred_time", "sales_event_id"))));
        return vo;
    }

    public List<SalesDocumentItem> items(SalesDocument row) {
        QueryWrapper<SalesDocumentItem> query = new QueryWrapper<SalesDocumentItem>()
            .eq("tenant_id", row.getTenantId()).eq("sales_document_id", row.getSalesDocumentId())
            .eq("del_flag", "0").orderByAsc("line_no", "sales_item_id");
        return access.ignoreTenant(() -> itemMapper.selectList(query));
    }

    private void copySummary(SalesDocument row, ProductionOrderVo vo) {
        vo.setSalesDocumentId(row.getSalesDocumentId());
        vo.setTenantId(row.getTenantId());
        vo.setOrderNo(row.getOrderNo());
        vo.setSourceType(row.getSourceType());
        vo.setSourceNo(row.getSourceNo());
        vo.setMerchantName(row.getMerchantName());
        vo.setCustomerName(row.getCustomerName());
        vo.setProjectName(row.getProjectName());
        vo.setPaymentMethod(row.getPaymentMethod());
        vo.setPaidTime(row.getPaidTime());
        vo.setProductionStatus(row.getProductionStatus());
        vo.setProductionStartTime(row.getProductionStartTime());
        vo.setProductionCompleteTime(row.getProductionCompleteTime());
    }

    private int value(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }
}
