package com.bocoo.dealer.quickorder.domain.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuickOrderItemSummaryVo {
    Long tenantId;
    Long quickOrderId;
    Integer itemTypeCount;
    Integer totalQuantity;
}
