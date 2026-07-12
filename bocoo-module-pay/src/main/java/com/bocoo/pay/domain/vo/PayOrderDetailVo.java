package com.bocoo.pay.domain.vo;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class PayOrderDetailVo {
    Long payOrderId;
    String payOrderNo;
    Long salesDocumentId;
    String salesOrderNo;
    Long payerTenantId;
    String merchantName;
    String customerName;
    String subject;
    Long price;
    String currency;
    String channelCode;
    Integer status;
    String channelOrderNo;
    LocalDateTime successTime;
    List<PayAttemptVo> attempts;
    List<PayWebhookSummaryVo> webhooks;
    PayReceivableSummaryVo receivable;
}
