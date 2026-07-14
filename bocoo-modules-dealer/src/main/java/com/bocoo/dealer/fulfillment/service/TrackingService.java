package com.bocoo.dealer.fulfillment.service;

import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingSummaryVo;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentAudience;

import java.util.List;

public interface TrackingService {
    List<TrackingEventVo> sync(Long shipmentId, FulfillmentAudience audience);

    List<TrackingEventVo> events(Long shipmentId, FulfillmentAudience audience);

    List<TrackingSummaryVo> summaries(List<Long> shipmentIds, FulfillmentAudience audience);
}
