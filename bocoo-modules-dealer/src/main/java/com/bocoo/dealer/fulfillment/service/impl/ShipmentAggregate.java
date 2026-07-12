package com.bocoo.dealer.fulfillment.service.impl;

public record ShipmentAggregate(
    String shipmentStatus,
    int totalQuantity,
    int dispatchedQuantity,
    int packageCount,
    boolean allReceived
) {
}
