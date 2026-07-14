package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.service.BusinessCreditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/credit-account")
public class BusinessCreditAccountController {
    private final BusinessCreditQueryService service;

    @SaCheckPermission("pay:credit:query")
    @GetMapping
    public R<CreditAccountSummaryVo> account() {
        return R.ok(service.account());
    }

    @SaCheckPermission("pay:credit:query")
    @GetMapping("/transactions")
    public TableDataInfo<CreditTransactionVo> transactions(CreditTransactionQueryBo query, PageQuery pageQuery) {
        return service.transactions(query, pageQuery);
    }
}
