package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayWebhookEvent;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayWebhookEventMapper;
import com.bocoo.pay.paypal.PayPalOrderResult;
import com.bocoo.pay.paypal.PayPalSandboxCredentials;
import com.bocoo.pay.paypal.PayPalWebhookRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PayPalResultSupport {
    private final PayWebhookEventMapper webhookMapper;
    private final PayPalSandboxCredentials credentials;

    void validateFacts(PayOrder order, PayPalOrderResult result) {
        if (!order.getPrice().equals(result.getPrice())
            || !StringUtils.equalsIgnoreCase(order.getCurrency(), result.getCurrency())) {
            throw new ServiceException("PayPal amount or currency does not match the payment order");
        }
    }

    void validatePayee(PayPalOrderResult result) {
        String expected = credentials.expectedMerchantId();
        if (StringUtils.isNotBlank(expected) && !StringUtils.equals(expected, result.getPayeeMerchantId())) {
            throw new ServiceException("PayPal payee does not match the configured merchant");
        }
    }

    PayWebhookEvent received(PayPalWebhookRequest request, PayOrderExtension attempt, PayOrder order) {
        PayWebhookEvent event = new PayWebhookEvent();
        event.setTenantId(order.getTenantId());
        event.setAppId(order.getAppId());
        event.setChannelId(attempt.getChannelId());
        event.setOrderId(order.getId());
        event.setExtensionId(attempt.getId());
        event.setChannelCode(PayChannelCode.PAYPAL.getCode());
        event.setChannelEventId(request.getEventId());
        event.setEventType(request.getEventType());
        event.setChannelOrderNo(request.getPaypalOrderId());
        event.setChannelCaptureNo(request.getCaptureId());
        event.setPrice(request.getPrice());
        event.setCurrency(request.getCurrency());
        event.setSignatureStatus("PENDING");
        event.setProcessStatus("PENDING");
        event.setRetryCount(0);
        event.setReceivedTime(TimeUtils.utcNow());
        return event;
    }

    void finish(PayWebhookEvent event, String status, String errorCode) {
        event.setProcessStatus(status);
        event.setErrorCode(errorCode);
        event.setProcessedTime(TimeUtils.utcNow());
        webhookMapper.updateById(event);
    }

    PayPalCheckoutVo checkout(PayOrder order, PayOrderExtension attempt, String paypalOrderId,
                              String captureId, String approvalUrl) {
        return PayPalCheckoutVo.builder().payOrderId(order.getId()).extensionId(attempt.getId())
            .paypalOrderId(paypalOrderId).captureId(captureId)
            .status(PayOrderStatus.SUCCESS.getStatus().equals(order.getStatus()) ? "COMPLETED" : "PROCESSING")
            .approvalUrl(approvalUrl == null ? attempt.getChannelExtras() : approvalUrl).build();
    }
}
