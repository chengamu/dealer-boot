package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditAccountQueryBo;
import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditFreezeBo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.service.MerchantCreditService;
import com.bocoo.pay.service.PayCreditQueryService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PlatformCreditAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformCreditAccountServiceImpl implements PlatformCreditAccountService {
    private final PayCreditQueryService queries;
    private final MerchantCreditService creditService;
    private final CreditAccountRepository accounts;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<CreditAccountSummaryVo> list(CreditAccountQueryBo query, PageQuery pageQuery) {
        requirePlatform();
        return queries.accounts(query, pageQuery);
    }

    @Override
    public CreditAccountSummaryVo detail(Long accountId) {
        requirePlatform();
        return CreditAccountSummaryVo.from(TenantContextHolder.callWithIgnore(() -> accounts.byId(accountId)));
    }

    @Override
    public TableDataInfo<CreditTransactionVo> transactions(Long accountId, CreditTransactionQueryBo query,
                                                           PageQuery pageQuery) {
        requirePlatform();
        TenantContextHolder.callWithIgnore(() -> accounts.byId(accountId));
        query.setCreditAccountId(accountId);
        return queries.transactions(query, pageQuery);
    }

    @Override
    public CreditAccountSummaryVo adjust(Long accountId, CreditAdjustBo bo) {
        requirePlatform();
        return CreditAccountSummaryVo.from(creditService.adjust(accountId, bo));
    }

    @Override
    public CreditAccountSummaryVo freeze(Long accountId, CreditFreezeBo bo) {
        requirePlatform();
        return CreditAccountSummaryVo.from(creditService.freeze(accountId, bo));
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }
}
