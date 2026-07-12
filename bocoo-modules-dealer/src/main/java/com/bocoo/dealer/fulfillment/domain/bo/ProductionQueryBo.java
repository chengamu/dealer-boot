package com.bocoo.dealer.fulfillment.domain.bo;

import lombok.Data;

@Data
public class ProductionQueryBo {
    private String orderNo;
    private String sourceNo;
    private String merchantName;
    private String customerName;
    private String productionStatus;
    private String paymentMethod;
}
