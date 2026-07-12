package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.service.PayOrderService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesPaymentOrderLinkerTest {
    @Mock private SalesDocumentMapper documentMapper;
    @Mock private PayOrderService orderService;
    private SalesPaymentOrderLinker linker;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
        TestSaTokenContext.install();
        linker = new SalesPaymentOrderLinker(documentMapper, orderService);
    }

    @Test
    void platformReadsMerchantPaymentWithoutWriting() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 1L, "finance");
        SalesDocument document = document();
        PayOrder order = payOrder();
        when(documentMapper.selectOne(any(), eq(false))).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return document;
        });
        when(orderService.getOrderByMerchantOrderId(300001L, "SO-1")).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return order;
        });

        SalesPaymentSnapshot snapshot = linker.getExisting(10L);

        assertThat(snapshot.document()).isSameAs(document);
        assertThat(snapshot.payOrder()).isSameAs(order);
        verify(orderService, never()).createOrder(any());
        verify(documentMapper, never()).update(any(), any());
    }

    @Test
    void orderInitializationCreatesAndLinksPaymentOrder() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        SalesDocument document = document();
        PayOrder order = payOrder();
        when(orderService.getOrderByMerchantOrderId(300001L, "SO-1")).thenReturn(null);
        when(orderService.createOrder(any())).thenReturn(order);
        when(documentMapper.update(isNull(), any())).thenReturn(1);

        linker.initialize(document);

        verify(orderService).createOrder(any());
        verify(documentMapper).update(isNull(), any());
        assertThat(document.getPayOrderId()).isEqualTo(20L);
        assertThat(document.getPayOrderNo()).isEqualTo("PO-1");
    }

    private SalesDocument document() {
        SalesDocument row = new SalesDocument();
        row.setSalesDocumentId(10L); row.setTenantId(300001L); row.setOrderNo("SO-1");
        row.setDocumentStatus("SUBMITTED"); row.setPaymentStatus("UNPAID");
        row.setTotalAmount(new BigDecimal("100.00")); row.setCurrencyCode("USD"); row.setDelFlag("0");
        return row;
    }

    private PayOrder payOrder() {
        PayOrder row = new PayOrder(); row.setId(20L); row.setNo("PO-1");
        row.setPayerTenantId(300001L); row.setMerchantOrderId("SO-1");
        return row;
    }
}
