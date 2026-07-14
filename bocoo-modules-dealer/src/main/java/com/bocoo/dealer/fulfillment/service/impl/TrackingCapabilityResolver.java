package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.tracking.TrackingCapability;
import com.bocoo.dealer.fulfillment.tracking.TrackingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrackingCapabilityResolver {
    private final List<TrackingProvider> providers;

    public TrackingProvider autoProvider(Shipment shipment) {
        TrackingProvider provider = providers.stream().filter(item -> item.supports(shipment.getCarrierCode()))
            .findFirst().orElseThrow(() ->
                ServiceException.ofMessageKey("dealer.fulfillment.trackingProviderMissing"));
        if (provider.capability() != TrackingCapability.AUTO) {
            throw ServiceException.ofMessageKey("dealer.fulfillment.trackingUnavailable");
        }
        return provider;
    }

    public TrackingCapability capability(Shipment shipment) {
        TrackingCapability capability = providers.stream()
            .filter(item -> item.supports(shipment.getCarrierCode())).findFirst()
            .map(TrackingProvider::capability).orElse(null);
        if (capability != null) return capability;
        return shipment.getTrackingNo() == null || shipment.getTrackingNo().isBlank()
            ? TrackingCapability.MANUAL : TrackingCapability.LINK_ONLY;
    }
}
