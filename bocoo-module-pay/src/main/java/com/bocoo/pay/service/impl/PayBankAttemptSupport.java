package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayChannelExtras;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PayBankAttemptSupport {
    private final PayOrderMapper orderMapper;
    private final PayOrderExtensionMapper extensionMapper;
    private final PayOperatorContext operator;
    private final PayChannelExtras extras;

    PayOrder requiredOrder(Long id) {
        PayOrder order = orderMapper.selectById(id);
        if (order == null) throw new ServiceException("Payment order does not exist");
        return order;
    }

    void validateFacts(PayOrder order, Long price, String currency) {
        if (!order.getPrice().equals(price) || !StringUtils.equalsIgnoreCase(order.getCurrency(), currency)) {
            throw new ServiceException("Bank transfer amount or currency does not match the payment order");
        }
    }

    PayOrderExtension newAttempt(PayOrder order, PayChannel channel, PayBankSubmitBo bo) {
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setTenantId(order.getTenantId());
        attempt.setPayerTenantId(order.getPayerTenantId());
        attempt.setPayeeTenantId(order.getPayeeTenantId());
        attempt.setNo(no());
        attempt.setOrderId(order.getId());
        attempt.setChannelId(channel.getId());
        attempt.setChannelCode(channel.getCode());
        attempt.setStatus(PayOrderStatus.PROCESSING.getStatus());
        attempt.setBankTransferStatus("PENDING_REVIEW");
        attempt.setBankPayerName(bo.getPayerName());
        attempt.setBankReferenceNo(bo.getBankReference());
        attempt.setBankTransferTime(bo.getTransferredTime());
        attempt.setBankDeclaredPrice(bo.getDeclaredPrice());
        attempt.setBankCurrency(bo.getCurrency());
        attempt.setBankProofMediaId(bo.getProofMediaId());
        attempt.setBankSubmittedTime(TimeUtils.utcNow());
        attempt.setChannelExtras(extras.bankRemark(bo.getRemark()));
        return attempt;
    }

    void markProcessing(PayOrder order, PayOrderExtension attempt, PayChannel channel) {
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getStatus, PayOrderStatus.PROCESSING.getStatus())
            .set(PayOrder::getChannelId, channel.getId()).set(PayOrder::getChannelCode, channel.getCode())
            .set(PayOrder::getExtensionId, attempt.getId()).eq(PayOrder::getId, order.getId())
            .eq(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus()));
        if (rows != 1) throw new ServiceException("Payment order was changed concurrently");
    }

    void reject(PayOrder order, PayOrderExtension attempt, String reason) {
        int attemptRows = extensionMapper.update(null, new LambdaUpdateWrapper<PayOrderExtension>()
            .set(PayOrderExtension::getStatus, PayOrderStatus.WAITING.getStatus())
            .set(PayOrderExtension::getBankTransferStatus, "REJECTED")
            .set(PayOrderExtension::getBankReviewedById, operator.userId())
            .set(PayOrderExtension::getBankReviewedBy, operator.username())
            .set(PayOrderExtension::getBankReviewedTime, TimeUtils.utcNow())
            .set(PayOrderExtension::getBankRejectReason, reason)
            .eq(PayOrderExtension::getId, attempt.getId())
            .eq(PayOrderExtension::getStatus, PayOrderStatus.PROCESSING.getStatus())
            .eq(PayOrderExtension::getBankTransferStatus, "PENDING_REVIEW"));
        int orderRows = orderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus())
            .eq(PayOrder::getId, order.getId()).eq(PayOrder::getExtensionId, attempt.getId())
            .eq(PayOrder::getStatus, PayOrderStatus.PROCESSING.getStatus()));
        if (attemptRows != 1 || orderRows != 1) throw new ServiceException("Bank transfer was changed concurrently");
        attempt.setStatus(PayOrderStatus.WAITING.getStatus());
        attempt.setBankTransferStatus("REJECTED");
        attempt.setBankRejectReason(reason);
    }

    private String no() {
        return "PE" + TimeUtils.utcNow().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
    }
}
