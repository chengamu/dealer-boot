package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentEvent;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.pay.api.PaymentSuccessCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesPaymentSuccessCallbackTest {
    static {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
    }
    @Mock SalesDocumentMapper documentMapper;
    @Mock SalesDocumentEventMapper eventMapper;

    @Test
    void confirmsUnpaidOrderAndRecordsEventOnce() {
        SalesDocument document = document("UNPAID");
        when(documentMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(document);
        when(documentMapper.update(any(), any())).thenReturn(1);
        SalesPaymentSuccessCallback callback = new SalesPaymentSuccessCallback(documentMapper, eventMapper);

        callback.confirmPayment(command());

        ArgumentCaptor<SalesDocumentEvent> event = ArgumentCaptor.forClass(SalesDocumentEvent.class);
        verify(eventMapper).insert(event.capture());
        assertThat(event.getValue().getEventType()).isEqualTo("PAYMENT_CONFIRMED");
        assertThat(event.getValue().getToStatus()).isEqualTo("PAID");
    }

    @Test
    void sameSuccessfulPaymentIsIdempotent() {
        SalesDocument document = document("PAID");
        when(documentMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(document);
        SalesPaymentSuccessCallback callback = new SalesPaymentSuccessCallback(documentMapper, eventMapper);

        callback.confirmPayment(command());

        verify(documentMapper, never()).update(any(), any());
        verify(eventMapper, never()).insert(any());
    }

    private SalesDocument document(String paymentStatus) {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(10L);
        row.setTenantId(300001L);
        row.setOrderNo("SO-1");
        row.setPayOrderId(1L);
        row.setPayOrderNo("PO-1");
        row.setDocumentStatus("SUBMITTED");
        row.setPaymentStatus(paymentStatus);
        row.setTotalAmount(new BigDecimal("878.40"));
        row.setCurrencyCode("USD");
        row.setDelFlag("0");
        return row;
    }

    private PaymentSuccessCommand command() {
        return PaymentSuccessCommand.builder().payOrderId(1L).payOrderNo("PO-1")
            .merchantOrderId("SO-1").payerTenantId(300001L).method("paypal")
            .paidPrice(87840L).currency("USD").channelOrderNo("CAP-1")
            .paidTime(LocalDateTime.of(2026, 7, 12, 2, 0)).build();
    }
}
