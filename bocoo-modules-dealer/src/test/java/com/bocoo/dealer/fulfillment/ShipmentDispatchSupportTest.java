package com.bocoo.dealer.fulfillment;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import com.bocoo.dealer.fulfillment.mapper.ShipmentItemMapper;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.service.impl.*;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentDispatchSupportTest {
    @Mock private ShipmentMapper shipmentMapper;
    @Mock private ShipmentItemMapper itemMapper;
    @Mock private SalesDocumentMapper documentMapper;
    @Mock private FulfillmentAccessSupport access;
    @Mock private ShipmentAllocationValidator validator;
    @Mock private ShipmentAggregationService aggregation;
    @Mock private FulfillmentEventRecorder events;
    private ShipmentDispatchSupport support;

    @BeforeEach
    void setUp() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Shipment.class);
        TableInfoHelper.initTableInfo(assistant, SalesDocument.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "shipper");
        lenient().when(access.ignoreTenant(any())).thenAnswer(invocation ->
            ((Supplier<?>) invocation.getArgument(0)).get());
        support = new ShipmentDispatchSupport(shipmentMapper, itemMapper, documentMapper,
            access, validator, aggregation, events);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void dispatchTransitionsDraftAndAggregatesOrderStatus() {
        Shipment shipment = shipment();
        SalesDocument document = document();
        ShipmentItem item = new ShipmentItem();
        item.setSalesItemId(11L);
        item.setQuantity(2);
        when(access.shipment(2L)).thenReturn(shipment);
        when(access.document(1L)).thenReturn(document);
        when(itemMapper.selectList(any())).thenReturn(List.of(item));
        when(shipmentMapper.selectCount(any())).thenReturn(0L);
        when(shipmentMapper.update(isNull(), any())).thenReturn(1);
        when(documentMapper.update(isNull(), any())).thenReturn(1);
        when(aggregation.aggregate(document)).thenReturn(
            new ShipmentAggregate("PARTIALLY_SHIPPED", 5, 2, 1, false));

        support.dispatch(2L);

        ArgumentCaptor<LambdaUpdateWrapper<Shipment>> shipmentUpdate = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(shipmentMapper).update(isNull(), shipmentUpdate.capture());
        assertThat(shipment.getStatus()).isEqualTo("DRAFT");
        assertThat(shipmentUpdate.getValue().getParamNameValuePairs().values()).contains("DISPATCHED");
        ArgumentCaptor<LambdaUpdateWrapper<SalesDocument>> documentUpdate = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(documentMapper).update(isNull(), documentUpdate.capture());
        assertThat(documentUpdate.getValue().getParamNameValuePairs().values())
            .contains("PARTIALLY_SHIPPED", 1, 2);
        verify(events).record(1L, 300001L, "SHIPMENT_DISPATCHED", "UNSHIPPED", "PARTIALLY_SHIPPED", "SHP-1");
    }

    @Test
    void fulfillmentOrderWritesReuseLifecycleLock() {
        assertLocks(ProductionServiceImpl.class, Set.of("start", "complete"));
        assertLocks(ShipmentCommandServiceImpl.class, Set.of("create", "update", "delete", "dispatch"));
        assertLocks(ReceiptServiceImpl.class, Set.of("confirm", "override"));
    }

    private void assertLocks(Class<?> type, Set<String> names) {
        for (Method method : type.getDeclaredMethods()) {
            if (!names.contains(method.getName())) continue;
            Lock4j lock = method.getAnnotation(Lock4j.class);
            assertThat(lock).as(type.getSimpleName() + "." + method.getName()).isNotNull();
            assertThat(lock.name()).isEqualTo("sales-document-lifecycle");
        }
    }

    private Shipment shipment() {
        Shipment row = new Shipment();
        row.setShipmentId(2L);
        row.setSalesDocumentId(1L);
        row.setTenantId(300001L);
        row.setShipmentNo("SHP-1");
        row.setCarrierCode("UPS");
        row.setCarrierName("UPS");
        row.setTrackingNo("TRACK-1");
        row.setStatus("DRAFT");
        row.setItemQuantity(2);
        row.setDelFlag("0");
        return row;
    }

    private SalesDocument document() {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(1L);
        row.setTenantId(300001L);
        row.setDocumentStatus("SUBMITTED");
        row.setProductionStatus("COMPLETED");
        row.setShipmentStatus("UNSHIPPED");
        row.setDelFlag("0");
        return row;
    }
}
