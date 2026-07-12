package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.bo.PaySupplementBo;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/admin")
public class PayBankAdminController {
    private final PayBankPaymentService service;

    @SaCheckPermission("pay:bank:review")
    @PostMapping("/bank/{extensionId}/review")
    public R<PayAttemptVo> review(@PathVariable Long extensionId,
                                  @Valid @RequestBody PayBankReviewBo bo) {
        return R.ok(PayAttemptVo.from(service.review(extensionId, bo)));
    }

    @SaCheckPermission("pay:order:supplement")
    @PostMapping("/order/{payOrderId}/supplement")
    public R<PayAttemptVo> supplement(@PathVariable Long payOrderId,
                                      @Valid @RequestBody PaySupplementBo bo) {
        return R.ok(PayAttemptVo.from(service.supplement(payOrderId, bo)));
    }
}
