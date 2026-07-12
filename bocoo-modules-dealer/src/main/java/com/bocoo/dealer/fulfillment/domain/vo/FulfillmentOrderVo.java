package com.bocoo.dealer.fulfillment.domain.vo;

import com.bocoo.dealer.domain.vo.SalesDocumentEventVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FulfillmentOrderVo extends ProductionOrderVo {
    private String documentStatus;
    private String paymentStatus;
    private String shipmentStatus;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private LocalDateTime deliveredTime;
    private List<FulfillmentItemVo> items;
    private List<ShipmentVo> shipments;
    private List<SalesDocumentEventVo> events;
}
