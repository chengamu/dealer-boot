package com.bocoo.pay.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.paypal.PayPalOrderResult;
import com.bocoo.pay.service.PayChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayPalCreateCoordinatorTest {
    @Mock PayPalAttemptSupport attempts;
    @Mock PayChannelService channelService;
    @Mock PayOrderExtensionMapper extensionMapper;

    @Test
    void reservesAndLinksAttemptBeforeExternalCall() {
        PayOrder order = order();
        PayChannel channel = channel();
        PayOrderExtension attempt = attempt();
        when(attempts.requiredOrder(1L)).thenReturn(order);
        when(attempts.current(order)).thenReturn(null);
        when(channelService.getEnabledChannel(1L, 7L, "paypal")).thenReturn(channel);
        when(attempts.create(order, channel)).thenReturn(attempt);

        PayPalCreateReservation reservation = service().reserve(1L);

        assertThat(reservation.attempt()).isSameAs(attempt);
        verify(attempts).linkWaiting(order, channel, attempt);
    }

    @Test
    void concurrentOrRetriedReservationReusesCurrentAttempt() {
        PayOrder order = order();
        PayOrderExtension attempt = attempt();
        when(attempts.requiredOrder(1L)).thenReturn(order);
        when(attempts.current(order)).thenReturn(attempt);

        assertThat(service().reserve(1L).attempt()).isSameAs(attempt);

        verify(attempts, never()).create(any(), any());
        verify(attempts, never()).linkWaiting(any(), any(), any());
    }

    @Test
    void reserveAndCompleteUseIndependentTransactionsAndOrderLocks() throws Exception {
        assertBoundary("reserve", Long.class);
        assertBoundary("complete", Long.class, Long.class, PayPalOrderResult.class);
    }

    private void assertBoundary(String methodName, Class<?>... types) throws Exception {
        Method method = PayPalCreateCoordinator.class.getDeclaredMethod(methodName, types);
        assertThat(method.getAnnotation(Lock4j.class)).isNotNull();
        assertThat(method.getAnnotation(Transactional.class).propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }

    private PayPalCreateCoordinator service() {
        return new PayPalCreateCoordinator(attempts, channelService, extensionMapper);
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(1L);
        order.setPayeeTenantId(1L);
        order.setAppId(7L);
        order.setStatus(PayOrderStatus.WAITING.getStatus());
        return order;
    }

    private PayChannel channel() {
        PayChannel channel = new PayChannel();
        channel.setId(9L);
        channel.setCode("paypal");
        return channel;
    }

    private PayOrderExtension attempt() {
        PayOrderExtension attempt = new PayOrderExtension();
        attempt.setId(20L);
        attempt.setChannelCode("paypal");
        attempt.setRequestId("REQ-1");
        return attempt;
    }
}
