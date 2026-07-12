package com.bocoo.dealer.fulfillment;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentItemBo;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentAccessSupport;
import com.bocoo.dealer.fulfillment.service.impl.ShipmentAggregate;
import com.bocoo.dealer.fulfillment.service.impl.ShipmentAggregationService;
import com.bocoo.dealer.fulfillment.service.impl.ShipmentAllocationValidator;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentQuantityAndAggregationTest {
    @Mock private SalesDocumentItemMapper salesItemMapper;
    @Mock private ShipmentMapper shipmentMapper;
    @Mock private ShipmentItemMapper shipmentItemMapper;
    @Mock private FulfillmentAccessSupport access;

    @BeforeEach
    void setUp() {
        lenient().when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void draftAndDispatchedAllocationsCannotExceedOrderQuantity() {
        SalesDocumentItem orderItem = orderItem(11L, 5);
        Shipment otherDraft = shipment(21L, "DRAFT", "PENDING");
        ShipmentItem allocated = shipmentItem(21L, 11L, 4);
        when(salesItemMapper.selectList(any())).thenReturn(List.of(orderItem));
        when(shipmentMapper.selectList(any())).thenReturn(List.of(otherDraft));
        when(shipmentItemMapper.selectList(any())).thenReturn(List.of(allocated));
        ShipmentItemBo request = new ShipmentItemBo();
        request.setSalesItemId(11L);
        request.setQuantity(2);
        ShipmentAllocationValidator validator = new ShipmentAllocationValidator(
            salesItemMapper, shipmentMapper, shipmentItemMapper, access);

        assertThatThrownBy(() -> validator.validate(document(), null, List.of(request)))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void aggregationDistinguishesPartialShippedAndDelivered() {
        SalesDocumentItem first = orderItem(11L, 3);
        SalesDocumentItem second = orderItem(12L, 2);
        Shipment sent = shipment(21L, "DISPATCHED", "PENDING");
        when(salesItemMapper.selectList(any())).thenReturn(List.of(first, second));
        when(shipmentMapper.selectList(any())).thenReturn(List.of(sent));
        when(shipmentItemMapper.selectList(any())).thenReturn(List.of(shipmentItem(21L, 11L, 3)));
        ShipmentAggregationService service = new ShipmentAggregationService(
            salesItemMapper, shipmentMapper, shipmentItemMapper, access);

        ShipmentAggregate partial = service.aggregate(document());
        assertThat(partial.shipmentStatus()).isEqualTo("PARTIALLY_SHIPPED");
        assertThat(partial.dispatchedQuantity()).isEqualTo(3);

        sent.setReceiptStatus("CONFIRMED");
        when(shipmentItemMapper.selectList(any())).thenReturn(List.of(
            shipmentItem(21L, 11L, 3), shipmentItem(21L, 12L, 2)));
        ShipmentAggregate delivered = service.aggregate(document());
        assertThat(delivered.shipmentStatus()).isEqualTo("DELIVERED");
        assertThat(delivered.allReceived()).isTrue();
    }

    private SalesDocument document() {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(1L);
        row.setTenantId(300001L);
        return row;
    }

    private SalesDocumentItem orderItem(Long id, int quantity) {
        SalesDocumentItem row = new SalesDocumentItem();
        row.setSalesItemId(id);
        row.setQuantity(quantity);
        return row;
    }

    private Shipment shipment(Long id, String status, String receiptStatus) {
        Shipment row = new Shipment();
        row.setShipmentId(id);
        row.setStatus(status);
        row.setReceiptStatus(receiptStatus);
        return row;
    }

    private ShipmentItem shipmentItem(Long shipmentId, Long salesItemId, int quantity) {
        ShipmentItem row = new ShipmentItem();
        row.setShipmentId(shipmentId);
        row.setSalesItemId(salesItemId);
        row.setQuantity(quantity);
        return row;
    }
}
