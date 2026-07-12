package com.bocoo.pay.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayChannelService;
import com.bocoo.pay.service.PayChannelExtras;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PaymentSuccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayBankPaymentServiceImplTest extends PayServiceTestSupport {
    @Mock PayOrderMapper orderMapper;
    @Mock PayOrderExtensionMapper extensionMapper;
    @Mock PayChannelService channelService;
    @Mock PaymentSuccessService successService;
    @Mock PayOperatorContext operator;
    @Mock PayChannelExtras channelExtras;
    PayBankAttemptSupport attempts;
    PayBankPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        attempts = new PayBankAttemptSupport(orderMapper, extensionMapper, operator, channelExtras);
        service = new PayBankPaymentServiceImpl(orderMapper, extensionMapper, channelService,
            successService, operator, attempts, channelExtras);
    }

    @Test
    void submitCreatesPendingReviewAttempt() {
        PayOrder order = order(PayOrderStatus.WAITING.getStatus());
        PayChannel channel = new PayChannel();
        channel.setId(8L);
        channel.setCode("bank_transfer");
        when(operator.tenantId()).thenReturn(300001L);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(channelService.getEnabledChannel(1L, 7L, "bank_transfer")).thenReturn(channel);
        when(extensionMapper.insert(any())).thenAnswer(invocation -> {
            ((PayOrderExtension) invocation.getArgument(0)).setId(21L);
            return 1;
        });
        when(orderMapper.update(any(), any())).thenReturn(1);
        when(channelExtras.bankRemark(any())).thenReturn("{}");

        PayOrderExtension result = service.submit(1L, submitBo());

        assertThat(result.getBankTransferStatus()).isEqualTo("PENDING_REVIEW");
        assertThat(result.getBankReferenceNo()).isEqualTo("BANK-REF-1");
        assertThat(result.getBankProofMediaId()).isEqualTo(99L);
    }

    @Test
    void rejectReturnsOrderToWaitingWithoutSuccessCallback() {
        PayOrder order = order(PayOrderStatus.PROCESSING.getStatus());
        order.setExtensionId(21L);
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setId(21L);
        attempt.setOrderId(1L);
        attempt.setStatus(PayOrderStatus.PROCESSING.getStatus());
        attempt.setBankTransferStatus("PENDING_REVIEW");
        attempt.setBankDeclaredPrice(87840L);
        attempt.setBankCurrency("USD");
        when(operator.isPlatform()).thenReturn(true);
        when(extensionMapper.selectById(21L)).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return attempt;
        });
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(extensionMapper.update(any(), any())).thenReturn(1);
        when(orderMapper.update(any(), any())).thenReturn(1);
        PayBankReviewBo bo = new PayBankReviewBo();
        bo.setApproved(false);
        bo.setReason("Reference not found");

        PayOrderExtension result = service.review(21L, bo);

        assertThat(result.getBankTransferStatus()).isEqualTo("REJECTED");
        assertThat(result.getStatus()).isEqualTo(PayOrderStatus.WAITING.getStatus());
        verify(successService, never()).confirm(any(), any(), any(), any(), any(), any());
    }

    private PayBankSubmitBo submitBo() {
        PayBankSubmitBo bo = new PayBankSubmitBo();
        bo.setPayerName("Buyer LLC");
        bo.setBankReference("BANK-REF-1");
        bo.setTransferredTime(LocalDateTime.of(2026, 7, 12, 1, 0));
        bo.setDeclaredPrice(87840L);
        bo.setCurrency("USD");
        bo.setProofMediaId(99L);
        return bo;
    }

    private PayOrder order(Integer status) {
        PayOrder order = new PayOrder();
        order.setId(1L);
        order.setTenantId(300001L);
        order.setPayerTenantId(300001L);
        order.setPayeeTenantId(1L);
        order.setAppId(7L);
        order.setPrice(87840L);
        order.setCurrency("USD");
        order.setStatus(status);
        return order;
    }
}
