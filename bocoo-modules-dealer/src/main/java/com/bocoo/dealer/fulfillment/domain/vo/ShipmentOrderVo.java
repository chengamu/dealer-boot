package com.bocoo.dealer.fulfillment.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShipmentOrderVo extends ProductionOrderVo {
    private String shipmentStatus;
    private Integer dispatchedQuantity;
    private Integer packageCount;
    private LocalDateTime shippedTime;
    private String carrierName;
    private String trackingNo;
}
