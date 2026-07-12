package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayWebhookEvent;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.domain.vo.PayWebhookSummaryVo;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.mapper.PayWebhookEventMapper;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PayOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayOrderDetailServiceImpl implements PayOrderDetailService {
    private final PayOrderMapper orderMapper;
    private final PayOrderExtensionMapper extensionMapper;
    private final PayWebhookEventMapper webhookMapper;
    private final MerchantReceivableMapper receivableMapper;
    private final PayOperatorContext operator;

    @Override
    public PayOrderDetailVo getDetail(Long payOrderId) {
        PayOrder order = TenantContextHolder.callWithIgnore(() -> orderMapper.selectById(payOrderId));
        if (order == null || (!operator.isPlatform() && !operator.tenantId().equals(order.getPayerTenantId()))) {
            throw new ServiceException("Payment order does not exist");
        }
        List<PayAttemptVo> attempts = TenantContextHolder.callWithIgnore(() -> extensionMapper.selectList(
            new LambdaQueryWrapper<PayOrderExtension>().eq(PayOrderExtension::getOrderId, payOrderId)
                .orderByDesc(PayOrderExtension::getId))).stream().map(PayAttemptVo::from).toList();
        List<PayWebhookSummaryVo> webhooks = TenantContextHolder.callWithIgnore(() -> webhookMapper.selectList(
            new LambdaQueryWrapper<PayWebhookEvent>().eq(PayWebhookEvent::getOrderId, payOrderId)
                .orderByDesc(PayWebhookEvent::getReceivedTime))).stream().map(this::webhook).toList();
        MerchantReceivable row = TenantContextHolder.callWithIgnore(() -> receivableMapper.selectOne(
            new LambdaQueryWrapper<MerchantReceivable>().eq(MerchantReceivable::getPayOrderId, payOrderId), false));
        return PayOrderDetailVo.builder().payOrderId(order.getId()).payOrderNo(order.getNo())
            .salesDocumentId(order.getSalesDocumentId()).salesOrderNo(order.getSalesOrderNo())
            .payerTenantId(order.getPayerTenantId()).merchantName(order.getMerchantName())
            .customerName(order.getCustomerName()).subject(order.getSubject()).price(order.getPrice())
            .currency(order.getCurrency()).channelCode(order.getChannelCode()).status(order.getStatus())
            .channelOrderNo(order.getChannelOrderNo()).successTime(order.getSuccessTime())
            .attempts(attempts).webhooks(webhooks).receivable(PayReceivableSummaryVo.from(row)).build();
    }

    private PayWebhookSummaryVo webhook(PayWebhookEvent row) {
        return PayWebhookSummaryVo.builder().channelEventId(row.getChannelEventId()).eventType(row.getEventType())
            .channelOrderNo(row.getChannelOrderNo()).channelCaptureNo(row.getChannelCaptureNo())
            .signatureStatus(row.getSignatureStatus()).processStatus(row.getProcessStatus())
            .errorCode(row.getErrorCode()).errorMessage(row.getErrorMessage())
            .receivedTime(row.getReceivedTime()).processedTime(row.getProcessedTime()).build();
    }

}
