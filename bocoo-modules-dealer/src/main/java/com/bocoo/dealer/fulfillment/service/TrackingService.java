package com.bocoo.dealer.fulfillment.service;

import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;

import java.util.List;

public interface TrackingService {
    List<TrackingEventVo> sync(Long shipmentId);

    List<TrackingEventVo> events(Long shipmentId);
}
