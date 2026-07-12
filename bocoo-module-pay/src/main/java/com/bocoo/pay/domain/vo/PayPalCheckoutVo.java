package com.bocoo.pay.domain.vo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayPalCheckoutVo {
    Long payOrderId;
    Long extensionId;
    String paypalOrderId;
    String captureId;
    String status;
    String approvalUrl;
}
