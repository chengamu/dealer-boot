package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.idempotent.annotation.RepeatSubmit;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.domain.bo.SalesPaymentBo;
import com.bocoo.dealer.domain.bo.SalesShipmentBo;
import com.bocoo.dealer.service.SalesDocumentLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/sales-documents")
public class SalesDocumentLifecycleController extends BaseController {
    private final SalesDocumentLifecycleService service;

    @SaCheckPermission("dealer:sales:quote") @PutMapping("/{id:\\d+}/quote")
    public R<Void> quote(@PathVariable Long id) { return toAjax(service.quote(id)); }
    @SaCheckPermission("dealer:sales:quote") @PutMapping("/{id:\\d+}/reopen")
    public R<Void> reopen(@PathVariable Long id) { return toAjax(service.reopen(id)); }
    @RepeatSubmit @SaCheckPermission("dealer:sales:submit") @PutMapping("/{id:\\d+}/submit")
    public R<String> submit(@PathVariable Long id) { return R.ok(service.submit(id)); }
    @SaCheckPermission("dealer:sales:cancel") @PutMapping("/{id:\\d+}/cancel")
    public R<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) { return toAjax(service.cancel(id, reason)); }
    @SaCheckPermission("dealer:sales:payment") @PutMapping("/{id:\\d+}/payment")
    public R<Void> payment(@PathVariable Long id, @Valid @RequestBody SalesPaymentBo bo) { return toAjax(service.confirmPayment(id, bo)); }
    @SaCheckPermission("dealer:sales:production") @PutMapping("/{id:\\d+}/production/start")
    public R<Void> start(@PathVariable Long id) { return toAjax(service.startProduction(id)); }
    @SaCheckPermission("dealer:sales:production") @PutMapping("/{id:\\d+}/production/complete")
    public R<Void> complete(@PathVariable Long id) { return toAjax(service.completeProduction(id)); }
    @SaCheckPermission("dealer:sales:shipment") @PutMapping("/{id:\\d+}/ship")
    public R<Void> ship(@PathVariable Long id, @Valid @RequestBody SalesShipmentBo bo) { return toAjax(service.ship(id, bo)); }
    @SaCheckPermission("dealer:sales:deliver") @PutMapping("/{id:\\d+}/deliver")
    public R<Void> deliver(@PathVariable Long id) { return toAjax(service.deliver(id)); }
}
