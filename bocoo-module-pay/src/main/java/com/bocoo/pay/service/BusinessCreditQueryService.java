package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.CreditTransactionVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;

public interface BusinessCreditQueryService {
    CreditAccountSummaryVo account();

    TableDataInfo<CreditTransactionVo> transactions(CreditTransactionQueryBo query, PageQuery pageQuery);

    TableDataInfo<PayReceivableSummaryVo> receivables(ReceivableQueryBo query, PageQuery pageQuery);

    PayReceivableSummaryVo receivable(Long receivableId);
}
