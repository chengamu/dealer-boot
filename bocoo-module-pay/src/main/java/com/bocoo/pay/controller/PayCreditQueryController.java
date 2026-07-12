package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditAccountQueryBo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.service.PayCreditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay/admin/credit")
public class PayCreditQueryController {
    private final PayCreditQueryService service;

    @SaCheckPermission("pay:credit:query")
    @GetMapping("/account/list")
    public TableDataInfo<CreditAccountSummaryVo> accounts(CreditAccountQueryBo query, PageQuery pageQuery) {
        return service.accounts(query, pageQuery);
    }

    @SaCheckPermission("pay:credit:query")
    @GetMapping("/transaction/list")
    public TableDataInfo<CreditTransactionVo> transactions(CreditTransactionQueryBo query, PageQuery pageQuery) {
        return service.transactions(query, pageQuery);
    }

    @SaCheckPermission("pay:credit:query")
    @GetMapping("/receivable/list")
    public TableDataInfo<PayReceivableSummaryVo> receivables(ReceivableQueryBo query, PageQuery pageQuery) {
        return service.receivables(query, pageQuery);
    }
}
