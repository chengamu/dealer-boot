package com.bocoo.dealer.fulfillment.domain.bo;

import lombok.Data;

@Data
public class ProductionQueryBo {
    private Long tenantId;
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private Long ownerUserId;
    private String orderNo;
    private String sourceNo;
    private String merchantName;
    private String customerName;
    private String productionStatus;
    private String paymentMethod;
}
