package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingSummaryVo;
import com.bocoo.dealer.fulfillment.service.FactoryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment/factory/tracking")
public class FactoryTrackingController {
    private final FactoryTrackingService service;

    @SaCheckPermission("dealer:fulfillment:factory:tracking:list")
    @GetMapping("/shipments/{id:\\d+}/events")
    public R<List<TrackingEventVo>> events(@PathVariable Long id) { return R.ok(service.events(id)); }

    @SaCheckPermission("dealer:fulfillment:factory:tracking:list")
    @GetMapping("/summaries")
    public R<List<TrackingSummaryVo>> summaries(@RequestParam List<Long> shipmentIds) {
        return R.ok(service.summaries(shipmentIds));
    }

    @Log(title = "Tracking Sync", businessType = BusinessType.OTHER)
    @SaCheckPermission("dealer:fulfillment:factory:tracking:sync")
    @PutMapping("/shipments/{id:\\d+}/sync")
    public R<List<TrackingEventVo>> sync(@PathVariable Long id) { return R.ok(service.sync(id)); }
}
