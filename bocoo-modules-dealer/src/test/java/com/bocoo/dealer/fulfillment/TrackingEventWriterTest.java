package com.bocoo.dealer.fulfillment;

import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.mapper.TrackingEventMapper;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentAccessSupport;
import com.bocoo.dealer.fulfillment.service.impl.TrackingEventWriter;
import com.bocoo.dealer.fulfillment.tracking.TrackingRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingEventWriterTest {
    @Mock private TrackingEventMapper mapper;
    @Mock private FulfillmentAccessSupport access;
    private TrackingEventWriter writer;

    @BeforeEach
    void setUp() {
        lenient().when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
        writer = new TrackingEventWriter(mapper, access);
    }

    @Test
    void providerEventIdMakesTrackingInsertIdempotent() {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(1L);
        shipment.setTenantId(300001L);
        shipment.setCarrierCode("UPS");
        shipment.setTrackingNo("TRACK-1");
        LocalDateTime time = LocalDateTime.of(2026, 7, 12, 1, 0);
        TrackingRecord existing = record("EV-1", time);
        TrackingRecord fresh = record("EV-2", time.plusHours(1));
        when(mapper.selectCount(any())).thenReturn(1L, 0L);
        when(mapper.insert(any())).thenReturn(1);

        LocalDateTime latest = writer.insertNew(shipment, List.of(existing, fresh));

        verify(mapper, times(1)).insert(any());
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
            com.bocoo.dealer.fulfillment.domain.entity.TrackingEvent>> queries = ArgumentCaptor.forClass(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class);
        verify(mapper, times(2)).selectCount(queries.capture());
        assertThat(queries.getAllValues()).allSatisfy(query -> {
            assertThat(query.getSqlSegment()).contains("shipment_id");
            assertThat(query.getParamNameValuePairs().values()).contains(1L);
        });
        assertThat(latest).isEqualTo(time.plusHours(1));
    }

    private TrackingRecord record(String id, LocalDateTime time) {
        return new TrackingRecord(id, "SCAN", "IN_TRANSIT", "scan", null,
            null, "Shanghai", time, "ref:" + id);
    }
}
