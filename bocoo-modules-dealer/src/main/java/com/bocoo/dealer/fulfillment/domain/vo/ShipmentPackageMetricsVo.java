package com.bocoo.dealer.fulfillment.domain.vo;

import lombok.Data;

@Data
public class ShipmentPackageMetricsVo {
    private Long tenantId;
    private Long salesDocumentId;
    private Integer packageCount;
    private Integer receivedPackageCount;
}
