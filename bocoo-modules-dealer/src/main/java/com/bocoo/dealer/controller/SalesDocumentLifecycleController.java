package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.service.SalesDocumentLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/sales-documents")
public class SalesDocumentLifecycleController extends BaseController {
    private final SalesDocumentLifecycleService service;

    @SaCheckPermission("dealer:sales:cancel") @PutMapping("/{id:\\d+}/cancel")
    public R<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) { return toAjax(service.cancel(id, reason)); }
}
