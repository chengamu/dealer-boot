package com.bocoo.dealer.fulfillment.tracking;

import com.bocoo.dealer.fulfillment.domain.entity.Shipment;

public interface TrackingProvider {
    boolean supports(String carrierCode);

    TrackingCapability capability();

    TrackingSnapshot fetch(Shipment shipment);
}
