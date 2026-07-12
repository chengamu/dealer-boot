package com.bocoo.dealer.fulfillment.tracking;

import java.util.List;

public record TrackingSnapshot(String status, List<TrackingRecord> events) {
    public TrackingSnapshot {
        events = events == null ? List.of() : List.copyOf(events);
    }
}
