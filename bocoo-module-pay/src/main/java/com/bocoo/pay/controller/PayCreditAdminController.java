package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditFreezeBo;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.service.MerchantCreditService;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/admin/credit")
public class PayCreditAdminController {
    private final MerchantCreditService service;

    @SaCheckPermission("platform:finance:credit:adjust")
    @PostMapping("/{accountId}/adjust")
    public R<CreditAccountSummaryVo> adjust(@PathVariable Long accountId,
                                             @Valid @RequestBody CreditAdjustBo bo) {
        return R.ok(CreditAccountSummaryVo.from(service.adjust(accountId, bo)));
    }

    @SaCheckPermission("platform:finance:credit:freeze")
    @PostMapping("/{accountId}/freeze")
    public R<CreditAccountSummaryVo> freeze(@PathVariable Long accountId,
                                             @Valid @RequestBody CreditFreezeBo bo) {
        return R.ok(CreditAccountSummaryVo.from(service.freeze(accountId, bo)));
    }

    @SaCheckPermission("platform:finance:receivable:repay")
    @PostMapping("/receivable/{receivableId}/repay")
    public R<PayReceivableSummaryVo> repay(@PathVariable Long receivableId,
                                            @NotBlank @RequestParam String reason) {
        return R.ok(PayReceivableSummaryVo.from(service.repay(receivableId, reason)));
    }
}
