package com.bocoo.dealer.fulfillment.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductionOrderVo {
    private Long salesDocumentId;
    private Long tenantId;
    private String orderNo;
    private String sourceType;
    private String sourceNo;
    private String merchantName;
    private String customerName;
    private String projectName;
    private String paymentMethod;
    private LocalDateTime paidTime;
    private String productionStatus;
    private LocalDateTime productionStartTime;
    private LocalDateTime productionCompleteTime;
    private Integer itemCount;
    private Integer totalQuantity;
}
