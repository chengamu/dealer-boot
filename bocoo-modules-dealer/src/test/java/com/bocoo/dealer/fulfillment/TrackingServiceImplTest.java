package com.bocoo.dealer.fulfillment;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.mapper.TrackingEventMapper;
import com.bocoo.dealer.fulfillment.service.impl.*;
import com.bocoo.dealer.fulfillment.tracking.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceImplTest {
    @Mock ShipmentMapper shipmentMapper;
    @Mock TrackingEventMapper eventMapper;
    @Mock TrackingEventWriter writer;
    @Mock FulfillmentAccessSupport access;
    @Mock TrackingProvider provider;

    @Test
    void batchSummaryReturnsExplicitProviderCapabilities() {
        Shipment automatic = shipment(1L, "UPS", "A-1");
        Shipment linked = shipment(2L, "OTHER", "B-1");
        when(provider.supports("UPS")).thenReturn(true);
        when(provider.supports("OTHER")).thenReturn(false);
        when(provider.capability()).thenReturn(TrackingCapability.AUTO);
        when(access.shipments(List.of(1L, 2L), FulfillmentAudience.FACTORY))
            .thenReturn(List.of(automatic, linked));
        when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
        TrackingEventVo latest = new TrackingEventVo();
        latest.setShipmentId(1L);
        when(eventMapper.selectVoList(any())).thenReturn(List.of(latest));

        var summaries = service().summaries(List.of(1L, 2L), FulfillmentAudience.FACTORY);

        assertThat(summaries).extracting(item -> item.getCapability())
            .containsExactly(TrackingCapability.AUTO, TrackingCapability.LINK_ONLY);
        assertThat(summaries.get(0).getLatestEvent()).isSameAs(latest);
    }

    @Test
    void nonAutoProviderCannotPretendToSynchronize() {
        Shipment shipment = shipment(1L, "UPS", "A-1");
        shipment.setStatus("DISPATCHED");
        when(access.shipment(1L, FulfillmentAudience.ADMIN)).thenReturn(shipment);
        when(provider.supports("UPS")).thenReturn(true);
        when(provider.capability()).thenReturn(TrackingCapability.MANUAL);

        assertThatThrownBy(() -> service().sync(1L, FulfillmentAudience.ADMIN))
            .isInstanceOf(ServiceException.class);
        verify(provider, never()).fetch(any());
    }

    private TrackingServiceImpl service() {
        return new TrackingServiceImpl(new TrackingCapabilityResolver(List.of(provider)),
            shipmentMapper, eventMapper, writer, access);
    }

    private Shipment shipment(Long id, String carrier, String trackingNo) {
        Shipment row = new Shipment();
        row.setShipmentId(id);
        row.setCarrierCode(carrier);
        row.setTrackingNo(trackingNo);
        return row;
    }
}
