package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.api.PaymentSuccessCallback;
import com.bocoo.pay.api.PaymentSuccessCommand;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PaymentSuccessService;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentSuccessServiceImpl implements PaymentSuccessService {
    private final PayOrderMapper orderMapper;
    private final PayOrderExtensionMapper extensionMapper;
    private final ObjectProvider<PaymentSuccessCallback> callbacks;
    private final PayOperatorContext operator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(Long extensionId, String channelOrderNo, Long paidPrice,
                           String currency, LocalDateTime paidTime, String safeNotifySummary) {
        PayOrderExtension attempt = requiredAttempt(extensionId);
        PayOrder order = requiredOrder(attempt.getOrderId());
        validateFacts(order, paidPrice, currency);
        LocalDateTime confirmedAt = paidTime == null ? TimeUtils.utcNow() : paidTime;

        int attemptRows = confirmAttempt(attempt, channelOrderNo, confirmedAt, safeNotifySummary);
        int orderRows = confirmOrder(order, channelOrderNo, confirmedAt);
        notifyBusiness(order, channelOrderNo, confirmedAt);
        return attemptRows > 0 || orderRows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean repair(Long payOrderId) {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
        return TenantContextHolder.callWithIgnore(() -> repairPlatform(payOrderId));
    }

    private boolean repairPlatform(Long payOrderId) {
        PayOrder order = requiredOrder(payOrderId);
        if (!PayOrderStatus.SUCCESS.getStatus().equals(order.getStatus())) {
            throw new ServiceException("Only successful payment orders can be repaired");
        }
        notifyBusiness(order, order.getChannelOrderNo(), order.getSuccessTime());
        return true;
    }

    private PayOrderExtension requiredAttempt(Long id) {
        PayOrderExtension attempt = extensionMapper.selectById(id);
        if (attempt == null) throw new ServiceException("Payment attempt does not exist");
        return attempt;
    }

    private PayOrder requiredOrder(Long id) {
        PayOrder order = orderMapper.selectById(id);
        if (order == null) throw new ServiceException("Payment order does not exist");
        return order;
    }

    private void validateFacts(PayOrder order, Long paidPrice, String currency) {
        if (!Objects.equals(order.getPrice(), paidPrice)) {
            throw new ServiceException("Payment amount does not match the order");
        }
        if (!StringUtils.equalsIgnoreCase(order.getCurrency(), currency)) {
            throw new ServiceException("Payment currency does not match the order");
        }
    }

    private int confirmAttempt(PayOrderExtension attempt, String channelOrderNo,
                               LocalDateTime paidTime, String summary) {
        return extensionMapper.update(null, new LambdaUpdateWrapper<PayOrderExtension>()
            .set(PayOrderExtension::getStatus, PayOrderStatus.SUCCESS.getStatus())
            .set(PayOrderExtension::getChannelCaptureNo, channelOrderNo)
            .set(PayOrderExtension::getSuccessTime, paidTime)
            .set(PayOrderExtension::getChannelNotifyData, summary)
            .eq(PayOrderExtension::getId, attempt.getId())
            .in(PayOrderExtension::getStatus, PayOrderStatus.WAITING.getStatus(), PayOrderStatus.PROCESSING.getStatus()));
    }

    private int confirmOrder(PayOrder order, String channelOrderNo, LocalDateTime paidTime) {
        return orderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getStatus, PayOrderStatus.SUCCESS.getStatus())
            .set(PayOrder::getSuccessTime, paidTime)
            .set(PayOrder::getChannelOrderNo, channelOrderNo)
            .eq(PayOrder::getId, order.getId())
            .in(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus(), PayOrderStatus.PROCESSING.getStatus()));
    }

    private void notifyBusiness(PayOrder order, String channelOrderNo, LocalDateTime paidTime) {
        PaymentSuccessCommand command = PaymentSuccessCommand.builder()
            .payOrderId(order.getId()).payOrderNo(order.getNo())
            .merchantOrderId(order.getMerchantOrderId()).payerTenantId(order.getPayerTenantId())
            .method(order.getChannelCode()).paidPrice(order.getPrice()).currency(order.getCurrency())
            .channelOrderNo(channelOrderNo).paidTime(paidTime).build();
        callbacks.orderedStream().forEach(callback -> callback.confirmPayment(command));
    }
}
