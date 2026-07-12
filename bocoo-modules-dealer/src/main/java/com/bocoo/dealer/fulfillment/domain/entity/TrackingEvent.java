package com.bocoo.dealer.fulfillment.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_tracking_event")
public class TrackingEvent extends BaseEntity {
    @TableId(value = "tracking_event_id")
    private Long trackingEventId;
    private Long tenantId;
    private Long shipmentId;
    private String carrierCode;
    private String trackingNo;
    private String providerEventId;
    private String eventCode;
    private String eventStatus;
    private String descriptionOriginal;
    private String descriptionCn;
    private String descriptionEn;
    private String location;
    private LocalDateTime occurredTime;
    private String source;
    private LocalDateTime receivedTime;
    private String rawDataRef;
}
