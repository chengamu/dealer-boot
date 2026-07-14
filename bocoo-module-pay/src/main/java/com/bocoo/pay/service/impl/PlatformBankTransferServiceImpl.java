package com.bocoo.pay.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentScopePage;
import com.bocoo.pay.api.PlatformPaymentPageResolver;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.vo.BankTransferSummaryVo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PlatformBankTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformBankTransferServiceImpl implements PlatformBankTransferService {
    private final PayOrderExtensionMapper extensionMapper;
    private final PayOrderMapper orderMapper;
    private final PayBankPaymentService bankPaymentService;
    private final PaymentDocumentScopeSupport scopes;
    private final PlatformPaymentPageResolver pageResolver;
    private final BankTransferQuerySupport queries;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<BankTransferSummaryVo> list(BankTransferQueryBo query, PageQuery pageQuery) {
        requirePlatform();
        PaymentScopePage scopePage = pageResolver.pageBankTransferIds(query, pageQuery);
        return queries.pageByIds(scopePage);
    }

    @Override
    public BankTransferSummaryVo detail(Long extensionId) {
        requirePlatform();
        PayOrderExtension row = TenantContextHolder.callWithIgnore(() -> extensionMapper.selectById(extensionId));
        if (row == null) throw new ServiceException("Bank transfer does not exist");
        PayOrder order = TenantContextHolder.callWithIgnore(() -> orderMapper.selectById(row.getOrderId()));
        if (order == null) throw new ServiceException("Payment order does not exist");
        var facts = scopes.required().resolveFacts(List.of(order.getSalesDocumentId()));
        return BankTransferSummaryVo.from(row, order, facts.get(order.getSalesDocumentId()));
    }

    @Override
    public PayAttemptVo review(Long extensionId, PayBankReviewBo bo) {
        requirePlatform();
        return PayAttemptVo.from(bankPaymentService.review(extensionId, bo));
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }
}
