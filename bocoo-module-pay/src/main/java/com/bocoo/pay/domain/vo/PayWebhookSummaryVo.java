package com.bocoo.pay.domain.vo;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class PayWebhookSummaryVo {
    String channelEventId;
    String eventType;
    String channelOrderNo;
    String channelCaptureNo;
    String signatureStatus;
    String processStatus;
    String errorCode;
    String errorMessage;
    LocalDateTime receivedTime;
    LocalDateTime processedTime;
}
