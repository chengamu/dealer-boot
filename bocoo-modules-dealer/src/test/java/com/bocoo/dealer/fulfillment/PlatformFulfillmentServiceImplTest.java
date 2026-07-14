package com.bocoo.dealer.fulfillment;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.fulfillment.service.ProductionService;
import com.bocoo.dealer.fulfillment.service.ReceiptService;
import com.bocoo.dealer.fulfillment.service.ShipmentQueryService;
import com.bocoo.dealer.fulfillment.service.TrackingService;
import com.bocoo.dealer.fulfillment.service.impl.PlatformFulfillmentServiceImpl;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PlatformFulfillmentServiceImplTest {
    @Mock ShipmentQueryService shipmentQueryService;
    @Mock ProductionService productionService;
    @Mock TrackingService trackingService;
    @Mock ReceiptService receiptService;
    PlatformFulfillmentServiceImpl service;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        service = new PlatformFulfillmentServiceImpl(shipmentQueryService, productionService,
            trackingService, receiptService, new PlatformSalesGuard());
    }

    @Test
    void merchantCannotEnterAnyPlatformFulfillmentOperation() {
        assertThatThrownBy(() -> service.queryPage(null, null)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.detail(1L)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.trackingEvents(1L)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.trackingSummaries(List.of(1L))).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.syncTracking(1L)).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.overrideReceipt(1L, "repair")).isInstanceOf(ServiceException.class);

        verifyNoInteractions(shipmentQueryService, productionService, trackingService, receiptService);
    }
}
