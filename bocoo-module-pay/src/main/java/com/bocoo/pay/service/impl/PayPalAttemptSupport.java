package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PayPalAttemptSupport {
    private final PayOrderMapper orderMapper;
    private final PayOrderExtensionMapper extensionMapper;

    PayOrder requiredOrder(Long id) {
        PayOrder order = orderMapper.selectById(id);
        if (order == null) throw new ServiceException("Payment order does not exist");
        return order;
    }

    PayOrderExtension current(PayOrder order) {
        return order.getExtensionId() == null ? null : extensionMapper.selectById(order.getExtensionId());
    }

    PayOrderExtension create(PayOrder order, PayChannel channel) {
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setTenantId(order.getTenantId());
        attempt.setPayerTenantId(order.getPayerTenantId());
        attempt.setPayeeTenantId(order.getPayeeTenantId());
        attempt.setNo("PE" + TimeUtils.utcNow().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + com.baomidou.mybatisplus.core.toolkit.IdWorker.getId());
        attempt.setRequestId("create-" + attempt.getNo());
        attempt.setOrderId(order.getId());
        attempt.setChannelId(channel.getId());
        attempt.setChannelCode(channel.getCode());
        attempt.setStatus(PayOrderStatus.WAITING.getStatus());
        attempt.setChannelExtras("{}");
        extensionMapper.insert(attempt);
        return attempt;
    }

    void linkWaiting(PayOrder order, PayChannel channel, PayOrderExtension attempt) {
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getChannelId, channel.getId()).set(PayOrder::getChannelCode, channel.getCode())
            .set(PayOrder::getExtensionId, attempt.getId()).eq(PayOrder::getId, order.getId())
            .eq(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus()).isNull(PayOrder::getExtensionId));
        if (rows != 1) throw new ServiceException("Payment order was changed concurrently");
        order.setChannelId(channel.getId());
        order.setChannelCode(channel.getCode());
        order.setExtensionId(attempt.getId());
    }

    void markProcessing(PayOrder order, PayOrderExtension attempt) {
        if (PayOrderStatus.PROCESSING.getStatus().equals(order.getStatus())) return;
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getStatus, PayOrderStatus.PROCESSING.getStatus())
            .eq(PayOrder::getId, order.getId()).eq(PayOrder::getExtensionId, attempt.getId())
            .eq(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus()));
        if (rows != 1) throw new ServiceException("Payment order was changed concurrently");
        order.setStatus(PayOrderStatus.PROCESSING.getStatus());
    }

    PayOrderExtension requiredPaypal(PayOrder order, String paypalOrderId) {
        PayOrderExtension attempt = current(order);
        if (attempt == null || !PayChannelCode.PAYPAL.getCode().equals(attempt.getChannelCode())
            || !StringUtils.equals(attempt.getChannelOrderNo(), paypalOrderId)) {
            throw new ServiceException("PayPal order does not match the current payment attempt");
        }
        return attempt;
    }
}
