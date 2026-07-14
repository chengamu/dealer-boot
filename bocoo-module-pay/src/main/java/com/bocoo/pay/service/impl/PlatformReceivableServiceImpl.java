package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.CreditRepayBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.service.MerchantCreditService;
import com.bocoo.pay.service.PayCreditQueryService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PlatformReceivableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformReceivableServiceImpl implements PlatformReceivableService {
    private final PayCreditQueryService queries;
    private final MerchantCreditService creditService;
    private final MerchantReceivableMapper mapper;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<PayReceivableSummaryVo> list(ReceivableQueryBo query, PageQuery pageQuery) {
        requirePlatform();
        return queries.receivables(query, pageQuery);
    }

    @Override
    public PayReceivableSummaryVo detail(Long receivableId) {
        requirePlatform();
        MerchantReceivable row = TenantContextHolder.callWithIgnore(() -> mapper.selectById(receivableId));
        if (row == null) throw new ServiceException("Receivable does not exist");
        return PayReceivableSummaryVo.from(row);
    }

    @Override
    public PayReceivableSummaryVo repay(Long receivableId, CreditRepayBo bo) {
        requirePlatform();
        return PayReceivableSummaryVo.from(creditService.repay(receivableId, bo));
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }
}
