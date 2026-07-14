package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.api.PaymentDocumentFilter;
import com.bocoo.pay.api.PaymentDocumentScopeResolver;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.BusinessPaymentQueryService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessPaymentQueryServiceImpl implements BusinessPaymentQueryService {
    private final PaymentOrderQuerySupport queries;
    private final PaymentDocumentScopeSupport scopes;
    private final PayOrderDetailService details;
    private final PayOrderMapper orderMapper;
    private final PayOperatorContext operator;

    @Override
    public TableDataInfo<PaymentOrderSummaryVo> list(PaymentOrderQueryBo query, PageQuery pageQuery) {
        PaymentDocumentScopeResolver resolver = scopes.required();
        List<Long> ids = resolver.accessibleDocumentIds(PaymentDocumentFilter.builder()
            .businessOrigin(query.getBusinessOrigin()).subjectId(query.getSubjectId()).keyword(query.getKeyword()).build());
        TableDataInfo<PaymentOrderSummaryVo> page = queries.page(query, pageQuery, ids);
        List<Long> pageIds = page.getRows().stream().map(PaymentOrderSummaryVo::getSalesDocumentId).distinct().toList();
        return queries.enrich(page, resolver.resolveFacts(pageIds));
    }

    @Override
    public PayOrderDetailVo detail(Long payOrderId) {
        var order = orderMapper.selectById(payOrderId);
        if (order == null) throw new ServiceException("Payment order does not exist");
        scopes.required().requireAccessible(order.getSalesDocumentId());
        if (!operator.tenantId().equals(order.getPayerTenantId())) {
            throw new ServiceException("Payment order does not belong to the current tenant");
        }
        return details.getDetail(payOrderId);
    }
}
