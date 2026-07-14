package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.*;
import com.bocoo.dealer.fulfillment.service.*;
import com.bocoo.dealer.scope.PlatformSalesGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformFulfillmentServiceImpl implements PlatformFulfillmentService {
    private final ShipmentQueryService shipmentQueryService;
    private final ProductionService productionService;
    private final TrackingService trackingService;
    private final ReceiptService receiptService;
    private final PlatformSalesGuard platformGuard;

    public TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery) {
        platformGuard.requirePlatform();
        return shipmentQueryService.queryPage(bo, pageQuery, FulfillmentAudience.ADMIN);
    }
    public FulfillmentOrderVo detail(Long id) {
        platformGuard.requirePlatform();
        return productionService.detail(id, FulfillmentAudience.ADMIN);
    }
    public List<TrackingEventVo> trackingEvents(Long id) {
        platformGuard.requirePlatform();
        return trackingService.events(id, FulfillmentAudience.ADMIN);
    }
    public List<TrackingSummaryVo> trackingSummaries(List<Long> ids) {
        platformGuard.requirePlatform();
        return trackingService.summaries(ids, FulfillmentAudience.ADMIN);
    }
    public List<TrackingEventVo> syncTracking(Long id) {
        platformGuard.requirePlatform();
        return trackingService.sync(id, FulfillmentAudience.ADMIN);
    }
    public Boolean overrideReceipt(Long id, String reason) {
        platformGuard.requirePlatform();
        return receiptService.override(id, reason);
    }
}
