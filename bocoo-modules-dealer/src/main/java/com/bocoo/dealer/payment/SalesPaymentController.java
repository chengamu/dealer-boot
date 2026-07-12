package com.bocoo.dealer.payment;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/sales-documents/{salesDocumentId}/payment")
public class SalesPaymentController {
    private final SalesPaymentService service;

    @SaCheckPermission("pay:order:query")
    @GetMapping
    public R<SalesPaymentVo> get(@PathVariable Long salesDocumentId) {
        return R.ok(service.getPayment(salesDocumentId));
    }

    @SaCheckPermission("pay:order:submit")
    @PostMapping("/paypal/create")
    public R<PayPalCheckoutVo> createPayPal(@PathVariable Long salesDocumentId) {
        return R.ok(service.createPayPal(salesDocumentId));
    }

    @SaCheckPermission("pay:order:submit")
    @PostMapping("/paypal/capture")
    public R<PayPalCheckoutVo> capturePayPal(@PathVariable Long salesDocumentId,
                                             @Valid @RequestBody PayPalCaptureBo bo) {
        return R.ok(service.capturePayPal(salesDocumentId, bo.getPaypalOrderId()));
    }

    @SaCheckPermission("pay:bank:submit")
    @PostMapping("/bank-transfer")
    public R<PayAttemptVo> submitBank(@PathVariable Long salesDocumentId,
                                      @Valid @RequestBody PayBankSubmitBo bo) {
        return R.ok(PayAttemptVo.from(service.submitBank(salesDocumentId, bo)));
    }

    @SaCheckPermission("pay:credit:use")
    @PostMapping("/credit")
    public R<PayReceivableSummaryVo> useCredit(@PathVariable Long salesDocumentId) {
        return R.ok(PayReceivableSummaryVo.from(service.useCredit(salesDocumentId)));
    }
}
