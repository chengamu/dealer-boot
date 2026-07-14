package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.vo.BankTransferSummaryVo;
import com.bocoo.pay.domain.vo.PayAttemptVo;

public interface PlatformBankTransferService {
    TableDataInfo<BankTransferSummaryVo> list(BankTransferQueryBo query, PageQuery pageQuery);

    BankTransferSummaryVo detail(Long extensionId);

    PayAttemptVo review(Long extensionId, PayBankReviewBo bo);
}
