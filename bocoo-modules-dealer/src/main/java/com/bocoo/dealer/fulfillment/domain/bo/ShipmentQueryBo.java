package com.bocoo.dealer.fulfillment.domain.bo;

import lombok.Data;

@Data
public class ShipmentQueryBo {
    private Long tenantId;
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private Long ownerUserId;
    private String orderNo;
    private String merchantName;
    private String customerName;
    private String shipmentStatus;
    private String carrierName;
    private String trackingNo;
}
