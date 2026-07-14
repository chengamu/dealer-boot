package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditRepayBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;

public interface PlatformReceivableService {
    TableDataInfo<PayReceivableSummaryVo> list(ReceivableQueryBo query, PageQuery pageQuery);

    PayReceivableSummaryVo detail(Long receivableId);

    PayReceivableSummaryVo repay(Long receivableId, CreditRepayBo bo);
}
