package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.fulfillment.domain.bo.ReceiptOverrideBo;
import com.bocoo.dealer.fulfillment.domain.vo.TrackingEventVo;
import com.bocoo.dealer.fulfillment.service.ReceiptService;
import com.bocoo.dealer.fulfillment.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment/shipments")
public class TrackingReceiptController extends BaseController {
    private final TrackingService trackingService;
    private final ReceiptService receiptService;

    @Log(title = "Tracking Sync", businessType = BusinessType.OTHER)
    @SaCheckPermission("dealer:fulfillment:tracking:sync")
    @PutMapping("/{id:\\d+}/sync-tracking")
    public R<List<TrackingEventVo>> sync(@PathVariable Long id) {
        return R.ok(trackingService.sync(id));
    }

    @SaCheckPermission("dealer:fulfillment:tracking:list")
    @GetMapping("/{id:\\d+}/tracking-events")
    public R<List<TrackingEventVo>> events(@PathVariable Long id) {
        return R.ok(trackingService.events(id));
    }

    @Log(title = "Package Receipt", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:receipt:confirm")
    @PutMapping("/{id:\\d+}/confirm-receipt")
    public R<Void> confirm(@PathVariable Long id) {
        return toAjax(receiptService.confirm(id));
    }

    @Log(title = "Package Receipt Override", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:receipt:override")
    @PutMapping("/{id:\\d+}/override-receipt")
    public R<Void> override(@PathVariable Long id, @Valid @RequestBody ReceiptOverrideBo bo) {
        return toAjax(receiptService.override(id, bo.getReason()));
    }
}
