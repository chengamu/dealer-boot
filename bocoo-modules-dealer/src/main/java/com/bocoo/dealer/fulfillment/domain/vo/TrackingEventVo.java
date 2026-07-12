package com.bocoo.dealer.fulfillment.domain.vo;

import com.bocoo.dealer.fulfillment.domain.entity.TrackingEvent;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = TrackingEvent.class)
public class TrackingEventVo implements Serializable {
    private Long trackingEventId;
    private Long shipmentId;
    private String providerEventId;
    private String eventCode;
    private String eventStatus;
    private String descriptionOriginal;
    private String descriptionCn;
    private String descriptionEn;
    private String location;
    private LocalDateTime occurredTime;
    private String source;
}
