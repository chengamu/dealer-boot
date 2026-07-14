package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditAccountQueryBo;
import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditFreezeBo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.service.PlatformCreditAccountService;
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
@RequestMapping("/platform-finance/credit-accounts")
public class PlatformCreditAccountController {
    private final PlatformCreditAccountService service;

    @SaCheckPermission("platform:finance:credit:list")
    @GetMapping("/list")
    public TableDataInfo<CreditAccountSummaryVo> list(CreditAccountQueryBo query, PageQuery pageQuery) {
        return service.list(query, pageQuery);
    }

    @SaCheckPermission("platform:finance:credit:query")
    @GetMapping("/{accountId}")
    public R<CreditAccountSummaryVo> detail(@PathVariable Long accountId) {
        return R.ok(service.detail(accountId));
    }

    @SaCheckPermission("platform:finance:credit:query")
    @GetMapping("/{accountId}/transactions")
    public TableDataInfo<CreditTransactionVo> transactions(@PathVariable Long accountId,
                                                            CreditTransactionQueryBo query, PageQuery pageQuery) {
        return service.transactions(accountId, query, pageQuery);
    }

    @SaCheckPermission("platform:finance:credit:adjust")
    @PostMapping("/{accountId}/adjust")
    public R<CreditAccountSummaryVo> adjust(@PathVariable Long accountId, @Valid @RequestBody CreditAdjustBo bo) {
        return R.ok(service.adjust(accountId, bo));
    }

    @SaCheckPermission("platform:finance:credit:freeze")
    @PostMapping("/{accountId}/freeze")
    public R<CreditAccountSummaryVo> freeze(@PathVariable Long accountId, @Valid @RequestBody CreditFreezeBo bo) {
        return R.ok(service.freeze(accountId, bo));
    }

    @SaCheckPermission("platform:finance:credit:freeze")
    @PostMapping("/{accountId}/unfreeze")
    public R<CreditAccountSummaryVo> unfreeze(@PathVariable Long accountId, @Valid @RequestBody CreditFreezeBo bo) {
        bo.setFrozen(false);
        return R.ok(service.freeze(accountId, bo));
    }
}
