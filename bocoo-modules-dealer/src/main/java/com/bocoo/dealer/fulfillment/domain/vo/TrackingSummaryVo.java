package com.bocoo.dealer.fulfillment.domain.vo;

import com.bocoo.dealer.fulfillment.tracking.TrackingCapability;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TrackingSummaryVo implements Serializable {
    private Long shipmentId;
    private TrackingCapability capability;
    private String trackingStatus;
    private LocalDateTime lastTrackingTime;
    private TrackingEventVo latestEvent;
}
