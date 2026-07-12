package com.bocoo.pay.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_webhook_event")
public class PayWebhookEvent extends BaseEntity {
    @TableId(value = "id")
    private Long id;
    private Long tenantId;
    private Long appId;
    private Long channelId;
    private Long orderId;
    private Long extensionId;
    private String channelCode;
    private String channelEventId;
    private String eventType;
    private String channelOrderNo;
    private String channelCaptureNo;
    private Long price;
    private String currency;
    private String signatureStatus;
    private String processStatus;
    private Integer retryCount;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime nextRetryTime;
    private String rawDataRef;
    private String eventSummary;
    private LocalDateTime receivedTime;
    private LocalDateTime processedTime;
}
