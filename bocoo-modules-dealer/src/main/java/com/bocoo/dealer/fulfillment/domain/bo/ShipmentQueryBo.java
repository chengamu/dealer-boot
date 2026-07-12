package com.bocoo.dealer.fulfillment.domain.bo;

import lombok.Data;

@Data
public class ShipmentQueryBo {
    private String orderNo;
    private String merchantName;
    private String customerName;
    private String shipmentStatus;
    private String carrierName;
    private String trackingNo;
}
