package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.CustomerQuoteDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/customer/quotes")
public class CustomerQuoteCalculationController {

    private final CustomerQuoteDraftService draftService;

    @SaCheckPermission("customer:quote:edit")
    @PostMapping("/calculate-item")
    public R<CustomerQuoteItemVo> calculateItem(@RequestParam(defaultValue = "EN_US") String quoteLanguage,
                                                @Validated @RequestBody CustomerQuoteItemBo bo) {
        return R.ok(draftService.calculateItem(bo, quoteLanguage));
    }

    @SaCheckPermission("customer:quote:edit")
    @PutMapping("/{id:\\d+}/calculate")
    public R<CustomerQuoteVo> calculateAll(@PathVariable Long id) {
        return R.ok(draftService.calculateAll(id));
    }
}
