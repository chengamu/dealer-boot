package com.bocoo.pay.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayWebhookEvent;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.mapper.PayWebhookEventMapper;
import com.bocoo.pay.paypal.PayPalCreateRequest;
import com.bocoo.pay.paypal.PayPalGateway;
import com.bocoo.pay.paypal.PayPalOrderResult;
import com.bocoo.pay.paypal.PayPalSandboxCredentials;
import com.bocoo.pay.paypal.PayPalWebhookRequest;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PaymentSuccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPalPaymentServiceImplTest extends PayServiceTestSupport {
    @Mock PayOrderMapper orderMapper;
    @Mock PayOrderExtensionMapper extensionMapper;
    @Mock PayWebhookEventMapper webhookMapper;
    @Mock PaymentSuccessService successService;
    @Mock PayPalGateway gateway;
    @Mock PayPalSandboxCredentials credentials;
    @Mock PayOperatorContext operator;
    @Mock PayPalCreateCoordinator createCoordinator;
    PayPalPaymentServiceImpl service;
    PayPalAttemptSupport attempts;
    PayPalResultSupport results;

    @BeforeEach
    void setUp() {
        attempts = new PayPalAttemptSupport(orderMapper, extensionMapper);
        results = new PayPalResultSupport(webhookMapper, credentials);
        service = new PayPalPaymentServiceImpl(extensionMapper, webhookMapper,
            successService, gateway, attempts, results, operator, createCoordinator);
    }

    @Test
    void createUsesFrozenAmountAndPersistsProcessingAttempt() {
        PayOrder order = order(PayOrderStatus.WAITING.getStatus());
        PayOrderExtension attempt = paypalAttempt();
        PayPalOrderResult gatewayResult = createdResult();
        when(createCoordinator.reserve(1L)).thenReturn(new PayPalCreateReservation(order, attempt));
        when(gateway.createOrder(any())).thenReturn(gatewayResult);
        when(createCoordinator.complete(1L, 20L, gatewayResult)).thenAnswer(invocation -> {
            attempt.setChannelOrderNo("PP-1");
            return attempt;
        });

        PayPalCheckoutVo result = service.create(1L);

        ArgumentCaptor<PayPalCreateRequest> request = ArgumentCaptor.forClass(PayPalCreateRequest.class);
        verify(gateway).createOrder(request.capture());
        assertThat(request.getValue().getPrice()).isEqualTo(87840L);
        assertThat(request.getValue().getCurrency()).isEqualTo("USD");
        assertThat(result.getPaypalOrderId()).isEqualTo("PP-1");
        assertThat(result.getExtensionId()).isEqualTo(20L);
    }

    @Test
    void createRetryReusesPersistedRequestIdAfterUncertainFailure() {
        PayOrder order = order(PayOrderStatus.WAITING.getStatus());
        PayOrderExtension attempt = paypalAttempt();
        PayPalOrderResult gatewayResult = createdResult();
        when(createCoordinator.reserve(1L)).thenReturn(new PayPalCreateReservation(order, attempt));
        when(gateway.createOrder(any())).thenThrow(new RuntimeException("timeout")).thenReturn(gatewayResult);
        when(createCoordinator.complete(1L, 20L, gatewayResult)).thenReturn(attempt);

        assertThatThrownBy(() -> service.create(1L)).isInstanceOf(RuntimeException.class);
        service.create(1L);

        ArgumentCaptor<PayPalCreateRequest> requests = ArgumentCaptor.forClass(PayPalCreateRequest.class);
        verify(gateway, org.mockito.Mockito.times(2)).createOrder(requests.capture());
        assertThat(requests.getAllValues()).extracting(PayPalCreateRequest::getRequestId)
            .containsExactly("REQ-1", "REQ-1");
        verify(createCoordinator).complete(1L, 20L, gatewayResult);
    }

    @Test
    void captureCompletedUsesUnifiedSuccessService() {
        PayOrder order = order(PayOrderStatus.PROCESSING.getStatus());
        order.setExtensionId(20L);
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setId(20L);
        attempt.setNo("PE-1");
        attempt.setRequestId("REQ-1");
        attempt.setChannelCode("paypal");
        attempt.setChannelOrderNo("PP-1");
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(extensionMapper.selectById(20L)).thenReturn(attempt);
        when(gateway.captureOrder("PP-1", "REQ-1-capture")).thenReturn(PayPalOrderResult.builder()
            .orderId("PP-1").captureId("CAP-1").status("COMPLETED")
            .price(87840L).currency("USD").payeeMerchantId("M-1").build());
        when(credentials.expectedMerchantId()).thenReturn("M-1");

        service.capture(1L, "PP-1");

        verify(successService).confirm(any(), org.mockito.ArgumentMatchers.eq("CAP-1"),
            org.mockito.ArgumentMatchers.eq(87840L), org.mockito.ArgumentMatchers.eq("USD"), any(), any());
    }

    @Test
    void verifiedCompletedWebhookConfirmsOnce() {
        PayOrder order = order(PayOrderStatus.PROCESSING.getStatus());
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setId(20L);
        attempt.setOrderId(1L);
        attempt.setChannelId(9L);
        attempt.setChannelCode("paypal");
        attempt.setChannelOrderNo("PP-1");
        when(extensionMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return attempt;
        });
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(gateway.verifyWebhook(any())).thenReturn(true);

        boolean handled = service.handleWebhook(webhook());

        assertThat(handled).isTrue();
        verify(successService).confirm(org.mockito.ArgumentMatchers.eq(20L),
            org.mockito.ArgumentMatchers.eq("CAP-1"), org.mockito.ArgumentMatchers.eq(87840L),
            org.mockito.ArgumentMatchers.eq("USD"), any(), any());
        verify(webhookMapper).insert(any(PayWebhookEvent.class));
    }

    @Test
    void duplicateWebhookDoesNotVerifyOrConfirmAgain() {
        PayWebhookEvent completed = new PayWebhookEvent();
        completed.setSignatureStatus("VERIFIED");
        completed.setProcessStatus("SUCCESS");
        when(webhookMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(completed);

        assertThat(service.handleWebhook(webhook())).isTrue();

        verify(gateway, never()).verifyWebhook(any());
        verify(successService, never()).confirm(any(), any(), any(), any(), any(), any());
    }

    @Test
    void failedSignatureEventCanBeVerifiedOnRetry() {
        PayWebhookEvent failed = new PayWebhookEvent();
        failed.setId(30L);
        failed.setSignatureStatus("FAILED");
        failed.setProcessStatus("FAILED");
        failed.setRetryCount(0);
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setId(20L);
        attempt.setOrderId(1L);
        attempt.setChannelId(9L);
        attempt.setChannelCode("paypal");
        attempt.setChannelOrderNo("PP-1");
        when(webhookMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(failed);
        when(extensionMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(attempt);
        when(orderMapper.selectById(1L)).thenReturn(order(PayOrderStatus.PROCESSING.getStatus()));
        when(gateway.verifyWebhook(any())).thenReturn(true);

        assertThat(service.handleWebhook(webhook())).isTrue();

        assertThat(failed.getRetryCount()).isEqualTo(1);
        assertThat(failed.getSignatureStatus()).isEqualTo("VERIFIED");
        assertThat(failed.getProcessStatus()).isEqualTo("SUCCESS");
        verify(successService).confirm(any(), any(), any(), any(), any(), any());
        verify(webhookMapper, never()).insert(any());
    }

    private PayPalWebhookRequest webhook() {
        return PayPalWebhookRequest.builder().eventId("EV-1").eventType("PAYMENT.CAPTURE.COMPLETED")
            .paypalOrderId("PP-1").captureId("CAP-1").price(87840L).currency("USD")
            .transmissionId("T-1").transmissionTime("2026-07-12T00:00:00Z")
            .transmissionSig("sig").certUrl("https://api.paypal.com/cert")
            .authAlgo("SHA256withRSA").rawBody("{}").build();
    }

    private PayOrder order(Integer status) {
        PayOrder order = new PayOrder();
        order.setId(1L);
        order.setTenantId(300001L);
        order.setPayerTenantId(300001L);
        order.setPayeeTenantId(1L);
        order.setAppId(7L);
        order.setNo("PO-1");
        order.setMerchantOrderId("SO-1");
        order.setSubject("Sales order SO-1");
        order.setPrice(87840L);
        order.setCurrency("USD");
        order.setStatus(status);
        return order;
    }

    private PayOrderExtension paypalAttempt() {
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setId(20L);
        attempt.setNo("PE-1");
        attempt.setRequestId("REQ-1");
        attempt.setChannelCode("paypal");
        return attempt;
    }

    private PayPalOrderResult createdResult() {
        return PayPalOrderResult.builder().orderId("PP-1").status("CREATED")
            .price(87840L).currency("USD").approvalUrl("https://sandbox.example/approve").build();
    }
}
