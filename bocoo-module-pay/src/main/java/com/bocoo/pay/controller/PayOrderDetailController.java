package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.service.PayOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/admin/order")
public class PayOrderDetailController {
    private final PayOrderDetailService service;

    @SaCheckPermission("platform:finance:payment:query")
    @GetMapping("/{payOrderId}")
    public R<PayOrderDetailVo> get(@PathVariable Long payOrderId) {
        return R.ok(service.getDetail(payOrderId));
    }
}
