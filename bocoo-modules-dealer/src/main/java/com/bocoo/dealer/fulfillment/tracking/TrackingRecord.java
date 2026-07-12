package com.bocoo.dealer.fulfillment.tracking;

import java.time.LocalDateTime;

public record TrackingRecord(
    String providerEventId,
    String eventCode,
    String eventStatus,
    String descriptionOriginal,
    String descriptionCn,
    String descriptionEn,
    String location,
    LocalDateTime occurredTime,
    String rawDataRef
) {
}
