package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.api.PlatformPaymentPageResolver;
import com.bocoo.pay.domain.bo.PaySupplementBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import com.bocoo.pay.service.PlatformPaymentOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformPaymentOrderServiceImpl implements PlatformPaymentOrderService {
    private final PaymentOrderQuerySupport queries;
    private final PaymentDocumentScopeSupport scopes;
    private final PlatformPaymentPageResolver pageResolver;
    private final PayOrderDetailService details;
    private final PayBankPaymentService bankPaymentService;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<PaymentOrderSummaryVo> list(PaymentOrderQueryBo query, PageQuery pageQuery) {
        requirePlatform();
        PaymentScopePage scopePage = pageResolver.pagePaymentOrderIds(query, pageQuery);
        TableDataInfo<PaymentOrderSummaryVo> page = queries.pageByIds(scopePage);
        List<Long> pageIds = page.getRows().stream().map(PaymentOrderSummaryVo::getSalesDocumentId).distinct().toList();
        return queries.enrich(page, scopes.required().resolveFacts(pageIds));
    }

    @Override
    public PayOrderDetailVo detail(Long payOrderId) {
        requirePlatform();
        return details.getDetail(payOrderId);
    }

    @Override
    public PayAttemptVo supplement(Long payOrderId, PaySupplementBo bo) {
        requirePlatform();
        return PayAttemptVo.from(bankPaymentService.supplement(payOrderId, bo));
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }
}
