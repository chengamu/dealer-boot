package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.service.PayPalPaymentService;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/admin/order/{payOrderId}")
public class PayRepairAdminController {
    private final PaymentSuccessService successService;
    private final PayPalPaymentService payPalService;

    @SaCheckPermission("pay:order:repair")
    @PostMapping("/repair")
    public R<Void> repair(@PathVariable Long payOrderId) {
        successService.repair(payOrderId);
        return R.ok();
    }

    @SaCheckPermission("pay:reconcile:execute")
    @PostMapping("/paypal/reconcile")
    public R<PayPalCheckoutVo> reconcile(@PathVariable Long payOrderId) {
        return R.ok(payPalService.reconcile(payOrderId));
    }
}
