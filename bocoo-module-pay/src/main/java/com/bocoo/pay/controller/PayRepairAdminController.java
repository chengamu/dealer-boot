package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.exception.ServiceException;
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

    @SaCheckPermission("platform:finance:reconciliation:repair")
    @PostMapping("/repair")
    public R<Void> repair(@PathVariable Long payOrderId) {
        throw new ServiceException("Use a reconciliation case to repair order payment");
    }

    @SaCheckPermission("platform:finance:reconciliation:channel")
    @PostMapping("/paypal/reconcile")
    public R<PayPalCheckoutVo> reconcile(@PathVariable Long payOrderId) {
        throw new ServiceException("Use a reconciliation case to reconcile payment channel");
    }
}
