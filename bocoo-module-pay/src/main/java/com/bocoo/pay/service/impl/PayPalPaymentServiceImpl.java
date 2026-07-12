package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayWebhookEvent;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayWebhookEventMapper;
import com.bocoo.pay.paypal.PayPalCreateRequest;
import com.bocoo.pay.paypal.PayPalGateway;
import com.bocoo.pay.paypal.PayPalOrderResult;
import com.bocoo.pay.paypal.PayPalSandboxCredentials;
import com.bocoo.pay.paypal.PayPalWebhookRequest;
import com.bocoo.pay.service.PayPalPaymentService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayPalPaymentServiceImpl implements PayPalPaymentService {
    private final PayOrderExtensionMapper extensionMapper;
    private final PayWebhookEventMapper webhookMapper;
    private final PaymentSuccessService successService;
    private final PayPalGateway gateway;
    private final PayPalAttemptSupport attempts;
    private final PayPalResultSupport results;
    private final PayOperatorContext operator;
    private final PayPalCreateCoordinator createCoordinator;

    @Override
    public PayPalCheckoutVo create(Long payOrderId) {
        PayPalCreateReservation reservation = createCoordinator.reserve(payOrderId);
        PayOrder order = reservation.order();
        PayOrderExtension attempt = reservation.attempt();
        if (com.bocoo.common.core.utils.StringUtils.isNotBlank(attempt.getChannelOrderNo())) {
            return results.checkout(order, attempt, attempt.getChannelOrderNo(), null, attempt.getChannelExtras());
        }
        PayPalOrderResult result = gateway.createOrder(PayPalCreateRequest.builder()
            .requestId(attempt.getRequestId()).invoiceId(order.getNo()).customId(order.getMerchantOrderId())
            .description(order.getSubject()).price(order.getPrice()).currency(order.getCurrency()).build());
        results.validateFacts(order, result);
        attempt = createCoordinator.complete(payOrderId, attempt.getId(), result);
        return results.checkout(order, attempt, result.getOrderId(), null, result.getApprovalUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayPalCheckoutVo capture(Long payOrderId, String paypalOrderId) {
        PayOrder order = attempts.requiredOrder(payOrderId);
        PayOrderExtension attempt = attempts.requiredPaypal(order, paypalOrderId);
        PayPalOrderResult result;
        try {
            result = gateway.captureOrder(paypalOrderId, attempt.getRequestId() + "-capture");
        } catch (RuntimeException uncertain) {
            result = gateway.getOrder(paypalOrderId);
        }
        results.validateFacts(order, result);
        results.validatePayee(result);
        if (result.isCompleted()) {
            attempt.setChannelCaptureNo(result.getCaptureId());
            extensionMapper.updateById(attempt);
            successService.confirm(attempt.getId(), result.getCaptureId(), result.getPrice(), result.getCurrency(),
                TimeUtils.utcNow(), "paypal_capture_completed");
            order.setStatus(PayOrderStatus.SUCCESS.getStatus());
        }
        return results.checkout(order, attempt, result.getOrderId(), result.getCaptureId(), result.getApprovalUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayPalCheckoutVo reconcile(Long payOrderId) {
        return TenantContextHolder.callWithIgnore(() -> reconcileAcrossTenants(payOrderId));
    }

    private PayPalCheckoutVo reconcileAcrossTenants(Long payOrderId) {
        PayOrder order = attempts.requiredOrder(payOrderId);
        if (!operator.isPlatform() && !operator.tenantId().equals(order.getPayerTenantId())) {
            throw new ServiceException("Payment order does not belong to the current tenant");
        }
        PayOrderExtension attempt = attempts.current(order);
        if (attempt == null || !PayChannelCode.PAYPAL.getCode().equals(attempt.getChannelCode())) {
            throw new ServiceException("Current payment attempt is not PayPal");
        }
        PayPalOrderResult result = gateway.getOrder(attempt.getChannelOrderNo());
        results.validateFacts(order, result);
        results.validatePayee(result);
        if (result.isCompleted()) {
            successService.confirm(attempt.getId(), result.getCaptureId(), result.getPrice(), result.getCurrency(),
                TimeUtils.utcNow(), "paypal_reconciled");
            order.setStatus(PayOrderStatus.SUCCESS.getStatus());
        }
        return results.checkout(order, attempt, result.getOrderId(), result.getCaptureId(), result.getApprovalUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleWebhook(PayPalWebhookRequest request) {
        return TenantContextHolder.callWithIgnore(() -> handleWebhookAcrossTenants(request));
    }

    private boolean handleWebhookAcrossTenants(PayPalWebhookRequest request) {
        PayWebhookEvent existing = webhookMapper.selectOne(new LambdaQueryWrapper<PayWebhookEvent>()
            .eq(PayWebhookEvent::getChannelEventId, request.getEventId()), false);
        if (isCompleted(existing)) return true;
        PayOrderExtension attempt = extensionMapper.selectOne(new LambdaQueryWrapper<PayOrderExtension>()
            .eq(PayOrderExtension::getChannelCode, PayChannelCode.PAYPAL.getCode())
            .eq(PayOrderExtension::getChannelOrderNo, request.getPaypalOrderId()), false);
        if (attempt == null) {
            throw new ServiceException("PayPal payment attempt does not exist");
        }
        PayOrder order = attempts.requiredOrder(attempt.getOrderId());
        PayWebhookEvent event = prepareEvent(existing, request, attempt, order);
        if (!gateway.verifyWebhook(request)) {
            event.setSignatureStatus("FAILED");
            results.finish(event, "FAILED", "INVALID_SIGNATURE");
            return false;
        }
        event.setSignatureStatus("VERIFIED");
        if ("PAYMENT.CAPTURE.COMPLETED".equals(request.getEventType())) {
            successService.confirm(attempt.getId(), request.getCaptureId(), request.getPrice(),
                request.getCurrency(), TimeUtils.utcNow(), "paypal_webhook_completed");
            results.finish(event, "SUCCESS", null);
        } else {
            results.finish(event, "IGNORED", null);
        }
        return true;
    }

    private boolean isCompleted(PayWebhookEvent event) {
        return event != null && "VERIFIED".equals(event.getSignatureStatus())
            && ("SUCCESS".equals(event.getProcessStatus()) || "IGNORED".equals(event.getProcessStatus()));
    }

    private PayWebhookEvent prepareEvent(PayWebhookEvent existing, PayPalWebhookRequest request,
                                         PayOrderExtension attempt, PayOrder order) {
        if (existing == null) {
            PayWebhookEvent event = results.received(request, attempt, order);
            webhookMapper.insert(event);
            return event;
        }
        existing.setTenantId(order.getTenantId());
        existing.setAppId(order.getAppId());
        existing.setChannelId(attempt.getChannelId());
        existing.setOrderId(order.getId());
        existing.setExtensionId(attempt.getId());
        existing.setEventType(request.getEventType());
        existing.setChannelOrderNo(request.getPaypalOrderId());
        existing.setChannelCaptureNo(request.getCaptureId());
        existing.setPrice(request.getPrice());
        existing.setCurrency(request.getCurrency());
        existing.setSignatureStatus("PENDING");
        existing.setProcessStatus("PROCESSING");
        existing.setRetryCount(existing.getRetryCount() == null ? 1 : existing.getRetryCount() + 1);
        existing.setErrorCode(null);
        existing.setErrorMessage(null);
        existing.setNextRetryTime(null);
        existing.setProcessedTime(null);
        existing.setReceivedTime(TimeUtils.utcNow());
        webhookMapper.updateById(existing);
        return existing;
    }

}
