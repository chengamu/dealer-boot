package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.bo.PaySupplementBo;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayBankStatus;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.service.PayChannelService;
import com.bocoo.pay.service.PayChannelExtras;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PayBankPaymentServiceImpl implements PayBankPaymentService {
    private final PayOrderMapper orderMapper;
    private final PayOrderExtensionMapper extensionMapper;
    private final PayChannelService channelService;
    private final PaymentSuccessService successService;
    private final PayOperatorContext operator;
    private final PayBankAttemptSupport attempts;
    private final PayChannelExtras channelExtras;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderExtension submit(Long payOrderId, PayBankSubmitBo bo) {
        PayOrder order = attempts.requiredOrder(payOrderId);
        if (!operator.tenantId().equals(order.getPayerTenantId())) {
            throw new ServiceException("Payment order does not belong to the current tenant");
        }
        if (!PayOrderStatus.isWaiting(order.getStatus())) {
            throw new ServiceException("Payment order is not waiting for a bank transfer");
        }
        attempts.validateFacts(order, bo.getDeclaredPrice(), bo.getCurrency());
        if (bo.getProofMediaId() == null || bo.getProofMediaId() <= 0) {
            throw new ServiceException("Bank transfer proof is required");
        }
        PayChannel channel = channelService.getEnabledChannel(order.getPayeeTenantId(), order.getAppId(),
            PayChannelCode.BANK_TRANSFER.getCode());
        PayOrderExtension attempt = attempts.newAttempt(order, channel, bo);
        extensionMapper.insert(attempt);
        attempts.markProcessing(order, attempt, channel);
        return attempt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderExtension review(Long extensionId, PayBankReviewBo bo) {
        requirePlatform();
        return TenantContextHolder.callWithIgnore(() -> reviewPlatform(extensionId, bo));
    }

    private PayOrderExtension reviewPlatform(Long extensionId, PayBankReviewBo bo) {
        PayOrderExtension attempt = extensionMapper.selectById(extensionId);
        if (attempt == null || !"PENDING_REVIEW".equals(attempt.getBankTransferStatus())) {
            throw new ServiceException("Bank transfer is not pending review");
        }
        PayOrder order = attempts.requiredOrder(attempt.getOrderId());
        attempts.validateFacts(order, attempt.getBankDeclaredPrice(), attempt.getBankCurrency());
        if (Boolean.TRUE.equals(bo.getApproved())) {
            attempt.setBankReviewedById(operator.userId());
            attempt.setBankReviewedBy(operator.username());
            attempt.setBankReviewedTime(TimeUtils.utcNow());
            attempt.setBankTransferStatus("SUCCESS");
            extensionMapper.updateById(attempt);
            successService.confirm(attempt.getId(), attempt.getBankReferenceNo(), attempt.getBankDeclaredPrice(),
                attempt.getBankCurrency(), TimeUtils.utcNow(), "bank_transfer_approved");
            attempt.setStatus(PayBankStatus.SUCCESS.getStatus());
            return attempt;
        }
        if (StringUtils.isBlank(bo.getReason())) throw new ServiceException("Rejection reason is required");
        attempts.reject(order, attempt, bo.getReason());
        return attempt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderExtension supplement(Long payOrderId, PaySupplementBo bo) {
        requirePlatform();
        return TenantContextHolder.callWithIgnore(() -> supplementPlatform(payOrderId, bo));
    }

    private PayOrderExtension supplementPlatform(Long payOrderId, PaySupplementBo bo) {
        PayOrder order = attempts.requiredOrder(payOrderId);
        if (PayOrderStatus.SUCCESS.getStatus().equals(order.getStatus())
            || PayOrderStatus.CLOSED.getStatus().equals(order.getStatus())) {
            throw new ServiceException("Payment order cannot be supplemented");
        }
        attempts.validateFacts(order, bo.getPrice(), bo.getCurrency());
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setTenantId(order.getTenantId());
        attempt.setPayerTenantId(order.getPayerTenantId());
        attempt.setPayeeTenantId(order.getPayeeTenantId());
        attempt.setNo("PE" + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + IdWorker.getId());
        attempt.setOrderId(order.getId());
        attempt.setChannelCode(bo.getMethod());
        attempt.setStatus(PayOrderStatus.PROCESSING.getStatus());
        PayChannel channel = channelService.getEnabledChannel(order.getPayeeTenantId(), order.getAppId(), bo.getMethod());
        attempt.setChannelId(channel.getId());
        attempt.setBankTransferStatus("SUCCESS");
        attempt.setBankReferenceNo(bo.getReference());
        attempt.setBankDeclaredPrice(bo.getPrice());
        attempt.setBankCurrency(bo.getCurrency());
        attempt.setBankTransferTime(bo.getPaidTime());
        attempt.setBankProofMediaId(bo.getProofMediaId());
        attempt.setBankRejectReason(bo.getReason());
        attempt.setChannelExtras(channelExtras.supplementReason(bo.getReason()));
        attempt.setBankReviewedById(operator.userId());
        attempt.setBankReviewedBy(operator.username());
        attempt.setBankReviewedTime(TimeUtils.utcNow());
        extensionMapper.insert(attempt);
        order.setChannelCode(bo.getMethod());
        order.setExtensionId(attempt.getId());
        order.setStatus(PayOrderStatus.PROCESSING.getStatus());
        orderMapper.updateById(order);
        successService.confirm(attempt.getId(), bo.getReference(), bo.getPrice(), bo.getCurrency(),
            bo.getPaidTime(), "manual_payment_supplemented");
        return attempt;
    }

    private void requirePlatform() {
        if (!operator.isPlatform()) throw new ServiceException("Platform tenant is required");
    }

}
