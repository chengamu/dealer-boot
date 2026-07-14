package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.fulfillment.domain.bo.ReceiptOverrideBo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.*;
import com.bocoo.dealer.fulfillment.service.PlatformFulfillmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment/admin")
public class PlatformFulfillmentController extends BaseController {
    private final PlatformFulfillmentService service;

    @SaCheckPermission("dealer:fulfillment:admin:list")
    @GetMapping("/list")
    public TableDataInfo<ShipmentOrderVo> list(ShipmentQueryBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("dealer:fulfillment:admin:query")
    @GetMapping("/orders/{id:\\d+}")
    public R<FulfillmentOrderVo> detail(@PathVariable Long id) { return R.ok(service.detail(id)); }

    @SaCheckPermission("dealer:fulfillment:admin:tracking")
    @GetMapping("/shipments/{id:\\d+}/tracking-events")
    public R<List<TrackingEventVo>> events(@PathVariable Long id) { return R.ok(service.trackingEvents(id)); }

    @SaCheckPermission("dealer:fulfillment:admin:tracking")
    @GetMapping("/tracking-summaries")
    public R<List<TrackingSummaryVo>> summaries(@RequestParam List<Long> shipmentIds) {
        return R.ok(service.trackingSummaries(shipmentIds));
    }

    @Log(title = "Tracking Sync", businessType = BusinessType.OTHER)
    @SaCheckPermission("dealer:fulfillment:admin:tracking:sync")
    @PutMapping("/shipments/{id:\\d+}/sync-tracking")
    public R<List<TrackingEventVo>> sync(@PathVariable Long id) { return R.ok(service.syncTracking(id)); }

    @Log(title = "Package Receipt Override", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:admin:receipt:override")
    @PutMapping("/shipments/{id:\\d+}/override-receipt")
    public R<Void> override(@PathVariable Long id, @Valid @RequestBody ReceiptOverrideBo bo) {
        return toAjax(service.overrideReceipt(id, bo.getReason()));
    }
}
