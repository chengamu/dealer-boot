package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.vo.SalesDocumentItemMetricsVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentPackageMetricsVo;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentPageMetricsLoaderTest {
    @Mock SalesDocumentItemMapper salesItemMapper;
    @Mock ShipmentMapper shipmentMapper;
    @Mock FulfillmentAccessSupport access;

    @Test
    void buildsDeliveredStatusFromGroupedDatabaseMetrics() {
        SalesDocument document = new SalesDocument();
        document.setTenantId(300001L);
        document.setSalesDocumentId(100L);
        SalesDocumentItemMetricsVo sales = new SalesDocumentItemMetricsVo();
        sales.setTenantId(300001L);
        sales.setSalesDocumentId(100L);
        sales.setItemCount(3);
        sales.setTotalQuantity(5);
        sales.setDispatchedQuantity(5);
        sales.setAllDispatched(true);
        ShipmentPackageMetricsVo packages = new ShipmentPackageMetricsVo();
        packages.setTenantId(300001L);
        packages.setSalesDocumentId(100L);
        packages.setPackageCount(2);
        packages.setReceivedPackageCount(2);
        when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
        when(salesItemMapper.selectPageMetrics(List.of(300001L), List.of(100L))).thenReturn(List.of(sales));
        when(shipmentMapper.selectPackageMetrics(List.of(300001L), List.of(100L))).thenReturn(List.of(packages));

        Map<ShipmentPageMetricsLoader.DocumentKey, ShipmentPageMetrics> result =
            new ShipmentPageMetricsLoader(salesItemMapper, shipmentMapper, access).load(List.of(document));

        ShipmentPageMetrics metrics = result.get(new ShipmentPageMetricsLoader.DocumentKey(300001L, 100L));
        assertThat(metrics.itemCount()).isEqualTo(3);
        assertThat(metrics.shipment().shipmentStatus()).isEqualTo("DELIVERED");
        assertThat(metrics.shipment().totalQuantity()).isEqualTo(5);
        assertThat(metrics.shipment().dispatchedQuantity()).isEqualTo(5);
        assertThat(metrics.shipment().packageCount()).isEqualTo(2);
    }
}
