package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingSummaryVo;
import com.bocoo.dealer.fulfillment.service.FactoryTrackingService;
import com.bocoo.dealer.fulfillment.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FactoryTrackingServiceImpl implements FactoryTrackingService {
    private final TrackingService delegate;

    public List<TrackingEventVo> events(Long id) { return delegate.events(id, FulfillmentAudience.FACTORY); }
    public List<TrackingSummaryVo> summaries(List<Long> ids) {
        return delegate.summaries(ids, FulfillmentAudience.FACTORY);
    }
    public List<TrackingEventVo> sync(Long id) { return delegate.sync(id, FulfillmentAudience.FACTORY); }
}
