package com.bocoo.dealer.fulfillment.service;

import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingSummaryVo;

import java.util.List;

public interface FactoryTrackingService {
    List<TrackingEventVo> events(Long shipmentId);
    List<TrackingSummaryVo> summaries(List<Long> shipmentIds);
    List<TrackingEventVo> sync(Long shipmentId);
}
