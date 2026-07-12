package com.bocoo.dealer.domain.vo;

import lombok.Data;

@Data
public class SalesDocumentItemMetricsVo {
    private Long tenantId;
    private Long salesDocumentId;
    private Integer itemCount;
    private Integer totalQuantity;
    private Integer dispatchedQuantity;
    private Boolean allDispatched;
}
