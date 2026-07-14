package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.*;
import com.bocoo.dealer.fulfillment.service.BusinessFulfillmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment/progress")
public class BusinessFulfillmentController extends BaseController {
    private final BusinessFulfillmentService service;

    @SaCheckPermission("dealer:fulfillment:progress:list")
    @GetMapping("/list")
    public TableDataInfo<ShipmentOrderVo> list(ShipmentQueryBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("dealer:fulfillment:progress:query")
    @GetMapping("/orders/{id:\\d+}")
    public R<FulfillmentOrderVo> detail(@PathVariable Long id) { return R.ok(service.detail(id)); }

    @SaCheckPermission("dealer:fulfillment:progress:query")
    @GetMapping("/orders/{id:\\d+}/shipments")
    public R<List<ShipmentVo>> shipments(@PathVariable Long id) { return R.ok(service.shipments(id)); }

    @SaCheckPermission("dealer:fulfillment:progress:tracking")
    @GetMapping("/shipments/{id:\\d+}/tracking-events")
    public R<List<TrackingEventVo>> events(@PathVariable Long id) { return R.ok(service.trackingEvents(id)); }

    @SaCheckPermission("dealer:fulfillment:progress:tracking")
    @GetMapping("/tracking-summaries")
    public R<List<TrackingSummaryVo>> summaries(@RequestParam List<Long> shipmentIds) {
        return R.ok(service.trackingSummaries(shipmentIds));
    }

    @Log(title = "Package Receipt", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:progress:receipt:confirm")
    @PutMapping("/shipments/{id:\\d+}/confirm-receipt")
    public R<Void> confirm(@PathVariable Long id) { return toAjax(service.confirmReceipt(id)); }
}
