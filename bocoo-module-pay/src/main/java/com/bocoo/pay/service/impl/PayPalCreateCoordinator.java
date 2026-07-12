package com.bocoo.pay.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.paypal.PayPalOrderResult;
import com.bocoo.pay.service.PayChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class PayPalCreateCoordinator {
    private final PayPalAttemptSupport attempts;
    private final PayChannelService channelService;
    private final PayOrderExtensionMapper extensionMapper;

    @Lock4j(name = "paypal-create-reserve", keys = {"#payOrderId"})
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PayPalCreateReservation reserve(Long payOrderId) {
        PayOrder order = attempts.requiredOrder(payOrderId);
        PayOrderExtension current = attempts.current(order);
        if (current != null) {
            if (!PayChannelCode.PAYPAL.getCode().equals(current.getChannelCode())) {
                throw new ServiceException("Payment order already uses another payment channel");
            }
            return new PayPalCreateReservation(order, current);
        }
        if (!PayOrderStatus.isWaiting(order.getStatus())) {
            throw new ServiceException("Payment order cannot create a PayPal order in its current status");
        }
        PayChannel channel = channelService.getEnabledChannel(order.getPayeeTenantId(), order.getAppId(),
            PayChannelCode.PAYPAL.getCode());
        PayOrderExtension attempt = attempts.create(order, channel);
        attempts.linkWaiting(order, channel, attempt);
        return new PayPalCreateReservation(order, attempt);
    }

    @Lock4j(name = "paypal-create-complete", keys = {"#payOrderId"})
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PayOrderExtension complete(Long payOrderId, Long attemptId, PayPalOrderResult result) {
        PayOrder order = attempts.requiredOrder(payOrderId);
        if (!attemptId.equals(order.getExtensionId())) {
            throw new ServiceException("PayPal payment attempt is no longer current");
        }
        PayOrderExtension attempt = extensionMapper.selectById(attemptId);
        if (attempt == null) throw new ServiceException("PayPal payment attempt does not exist");
        if (StringUtils.isNotBlank(attempt.getChannelOrderNo())) {
            if (!StringUtils.equals(attempt.getChannelOrderNo(), result.getOrderId())) {
                throw new ServiceException("PayPal idempotency returned a different order");
            }
        } else {
            int rows = extensionMapper.update(null, new LambdaUpdateWrapper<PayOrderExtension>()
                .eq(PayOrderExtension::getId, attemptId)
                .eq(PayOrderExtension::getRequestId, attempt.getRequestId())
                .isNull(PayOrderExtension::getChannelOrderNo)
                .set(PayOrderExtension::getChannelOrderNo, result.getOrderId())
                .set(PayOrderExtension::getChannelExtras, result.getApprovalUrl())
                .set(PayOrderExtension::getStatus, PayOrderStatus.PROCESSING.getStatus()));
            if (rows != 1) throw new ServiceException("PayPal payment attempt was changed concurrently");
            attempt.setChannelOrderNo(result.getOrderId());
            attempt.setChannelExtras(result.getApprovalUrl());
            attempt.setStatus(PayOrderStatus.PROCESSING.getStatus());
        }
        attempts.markProcessing(order, attempt);
        return attempt;
    }
}
