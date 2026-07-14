package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.*;
import com.bocoo.dealer.fulfillment.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessFulfillmentServiceImpl implements BusinessFulfillmentService {
    private final ShipmentQueryService shipmentQueryService;
    private final ProductionService productionService;
    private final TrackingService trackingService;
    private final ReceiptService receiptService;

    public TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery) {
        return shipmentQueryService.queryPage(bo, pageQuery, FulfillmentAudience.BUSINESS);
    }
    public FulfillmentOrderVo detail(Long id) {
        return productionService.detail(id, FulfillmentAudience.BUSINESS);
    }
    public List<ShipmentVo> shipments(Long id) {
        return shipmentQueryService.orderShipments(id, FulfillmentAudience.BUSINESS);
    }
    public List<TrackingEventVo> trackingEvents(Long id) {
        return trackingService.events(id, FulfillmentAudience.BUSINESS);
    }
    public List<TrackingSummaryVo> trackingSummaries(List<Long> ids) {
        return trackingService.summaries(ids, FulfillmentAudience.BUSINESS);
    }
    public Boolean confirmReceipt(Long id) {
        return receiptService.confirm(id);
    }
}
