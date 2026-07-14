package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditAccountQueryBo;
import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditFreezeBo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;

public interface PlatformCreditAccountService {
    TableDataInfo<CreditAccountSummaryVo> list(CreditAccountQueryBo query, PageQuery pageQuery);

    CreditAccountSummaryVo detail(Long accountId);

    TableDataInfo<CreditTransactionVo> transactions(Long accountId, CreditTransactionQueryBo query, PageQuery pageQuery);

    CreditAccountSummaryVo adjust(Long accountId, CreditAdjustBo bo);

    CreditAccountSummaryVo freeze(Long accountId, CreditFreezeBo bo);
}
