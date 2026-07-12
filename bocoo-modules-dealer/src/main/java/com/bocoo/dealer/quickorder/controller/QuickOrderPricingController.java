package com.bocoo.dealer.quickorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderItemBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderItemVo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.service.QuickOrderPricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/quick-orders")
public class QuickOrderPricingController extends BaseController {
    private final QuickOrderPricingService service;

    @SaCheckPermission("dealer:quick-order:edit")
    @PutMapping("/{id:\\d+}/calculate-item")
    public R<QuickOrderItemVo> calculateItem(@PathVariable Long id,
                                             @Valid @RequestBody QuickOrderItemBo bo) {
        return R.ok(service.calculateItem(id, bo));
    }

    @SaCheckPermission("dealer:quick-order:edit")
    @PutMapping("/{id:\\d+}/calculate")
    public R<QuickOrderVo> calculate(@PathVariable Long id) {
        return R.ok(service.calculateAll(id));
    }
}
