package com.bocoo.dealer.fulfillment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.service.impl.*;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceImplTest {
    @Mock private ShipmentMapper shipmentMapper;
    @Mock private SalesDocumentMapper documentMapper;
    @Mock private FulfillmentAccessSupport access;
    @Mock private ShipmentAggregationService aggregation;
    @Mock private FulfillmentEventRecorder events;
    private ReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Shipment.class);
        TableInfoHelper.initTableInfo(assistant, SalesDocument.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 8L, "merchant");
        lenient().when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
        service = new ReceiptServiceImpl(shipmentMapper, documentMapper, access, aggregation, events);
    }

    @Test
    void finalPackageCompletesOrderOnlyOnce() {
        Shipment shipment = shipment();
        SalesDocument document = document();
        when(access.shipment(2L, FulfillmentAudience.BUSINESS)).thenReturn(shipment);
        when(access.document(1L, FulfillmentAudience.BUSINESS)).thenReturn(document);
        when(shipmentMapper.update(isNull(), any())).thenReturn(1);
        when(documentMapper.update(isNull(), any())).thenReturn(1);
        when(aggregation.aggregate(document)).thenReturn(new ShipmentAggregate("DELIVERED", 5, 5, 2, true));

        service.confirm(2L);

        verify(events).record(1L, 300001L, "PACKAGE_RECEIVED", "PENDING", "CONFIRMED", null);
        verify(events).record(1L, 300001L, "ORDER_COMPLETED", "SUBMITTED", "COMPLETED", null);
    }

    @Test
    void overrideRequiresReasonAndRecordsIt() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "admin");
        assertThatThrownBy(() -> service.override(2L, " ")).isInstanceOf(ServiceException.class);

        Shipment shipment = shipment();
        SalesDocument document = document();
        when(access.shipment(2L, FulfillmentAudience.ADMIN)).thenReturn(shipment);
        when(access.document(1L, FulfillmentAudience.ADMIN)).thenReturn(document);
        when(shipmentMapper.update(isNull(), any())).thenReturn(1);
        when(aggregation.aggregate(document)).thenReturn(new ShipmentAggregate("SHIPPED", 5, 5, 2, false));

        service.override(2L, "merchant unavailable");

        verify(events).record(1L, 300001L, "PACKAGE_RECEIPT_OVERRIDDEN", "PENDING", "CONFIRMED",
            "merchant unavailable");
    }

    @Test
    void internalSalespersonCanConfirmOwnedTenantOneShipment() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "sales");
        Shipment shipment = shipment();
        shipment.setTenantId(1L);
        SalesDocument document = document();
        document.setTenantId(1L);
        when(access.shipment(2L, FulfillmentAudience.BUSINESS)).thenReturn(shipment);
        when(access.document(1L, FulfillmentAudience.BUSINESS)).thenReturn(document);
        when(shipmentMapper.update(isNull(), any())).thenReturn(1);
        when(aggregation.aggregate(document)).thenReturn(new ShipmentAggregate("SHIPPED", 5, 5, 2, false));

        service.confirm(2L);

        verify(events).record(1L, 1L, "PACKAGE_RECEIVED", "PENDING", "CONFIRMED", null);
    }

    private Shipment shipment() {
        Shipment row = new Shipment();
        row.setShipmentId(2L);
        row.setSalesDocumentId(1L);
        row.setTenantId(300001L);
        row.setStatus("DISPATCHED");
        row.setReceiptStatus("PENDING");
        row.setDelFlag("0");
        return row;
    }

    private SalesDocument document() {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(1L);
        row.setTenantId(300001L);
        row.setDocumentStatus("SUBMITTED");
        row.setShipmentStatus("SHIPPED");
        row.setDelFlag("0");
        return row;
    }
}
