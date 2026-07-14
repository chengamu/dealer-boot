package com.bocoo.dealer.fulfillment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import com.bocoo.dealer.fulfillment.mapper.ShipmentMapper;
import com.bocoo.dealer.fulfillment.service.impl.*;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentAccessSupportTest {
    @Mock SalesDocumentMapper documentMapper;
    @Mock ShipmentMapper shipmentMapper;
    @Mock FulfillmentBusinessScopeResolver resolver;

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void businessDocumentUsesOriginAndOwnerWithoutSalesStoreAuthorization() {
        when(resolver.resolve()).thenReturn(new FulfillmentBusinessScope(1L, "INTERNAL", null, 9L));
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(10L);
        when(documentMapper.selectOne(any(), eq(false))).thenReturn(row);
        FulfillmentAccessSupport access = spy(new FulfillmentAccessSupport(documentMapper, shipmentMapper, resolver));
        doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get()).when(access).ignoreTenant(any());

        access.document(10L, FulfillmentAudience.BUSINESS);

        ArgumentCaptor<QueryWrapper<SalesDocument>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(documentMapper).selectOne(query.capture(), eq(false));
        assertThat(query.getValue().getSqlSegment()).contains("tenant_id", "business_origin", "owner_user_id")
            .doesNotContain("sales_store_id", "dept_id");
        assertThat(query.getValue().getParamNameValuePairs().values()).contains(1L, "INTERNAL", 9L);
    }

    @Test
    void businessBatchAuthorizationLoadsOrdersOnce() {
        when(resolver.resolve()).thenReturn(new FulfillmentBusinessScope(1L, "INTERNAL", null, 9L));
        Shipment first = shipment(1L, 10L);
        Shipment second = shipment(2L, 11L);
        when(shipmentMapper.selectList(any())).thenReturn(List.of(first, second));
        SalesDocument firstDocument = new SalesDocument();
        firstDocument.setSalesDocumentId(10L);
        SalesDocument secondDocument = new SalesDocument();
        secondDocument.setSalesDocumentId(11L);
        when(documentMapper.selectList(any())).thenReturn(List.of(firstDocument, secondDocument));
        FulfillmentAccessSupport access = spy(new FulfillmentAccessSupport(documentMapper, shipmentMapper, resolver));
        doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get()).when(access).ignoreTenant(any());

        assertThat(access.shipments(List.of(1L, 2L), FulfillmentAudience.BUSINESS))
            .containsExactly(first, second);

        verify(documentMapper, times(1)).selectList(any());
        verify(documentMapper, never()).selectOne(any(), anyBoolean());
    }

    @Test
    void internalSalespersonScopeUsesTenantOneOwnership() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9L, "sales");

        FulfillmentBusinessScope scope = new FulfillmentBusinessScopeResolver().resolve();

        assertThat(scope.tenantId()).isEqualTo(1L);
        assertThat(scope.businessOrigin()).isEqualTo("INTERNAL");
        assertThat(scope.deptId()).isNull();
        assertThat(scope.ownerUserId()).isEqualTo(9L);
    }

    private Shipment shipment(Long id, Long documentId) {
        Shipment row = new Shipment();
        row.setShipmentId(id);
        row.setSalesDocumentId(documentId);
        return row;
    }
}
