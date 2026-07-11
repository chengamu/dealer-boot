package com.bocoo.dealer.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.domain.bo.SalesPaymentBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesDocumentLifecycleServiceTest {
    @Mock private SalesDocumentMapper mapper;
    @Mock private SalesDocumentItemWriter writer;
    @Mock private SalesDocumentEventRecorder events;
    private SalesDocumentLifecycleServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 1L, "merchant");
        service = new SalesDocumentLifecycleServiceImpl(mapper, writer, events);
    }

    @Test
    void repeatedSubmitReturnsExistingOrderNumber() {
        SalesDocument row = row(); row.setDocumentStatus("SUBMITTED"); row.setOrderNo("SO-100");
        when(mapper.selectOne(any(), eq(false))).thenReturn(row);
        assertThat(service.submit(1L)).isEqualTo("SO-100");
        verifyNoInteractions(writer);
    }

    @Test
    void paymentAmountMustMatchTotal() {
        SalesDocument row = row(); when(mapper.selectOne(any(), eq(false))).thenReturn(row);
        SalesPaymentBo bo = new SalesPaymentBo(); bo.setPaymentMethod("BANK_TRANSFER");
        bo.setPaymentReference("REF-1"); bo.setPaidAmount(new BigDecimal("99"));
        platformLogin();
        assertThatThrownBy(() -> service.confirmPayment(1L, bo)).isInstanceOf(ServiceException.class);
    }

    @Test
    void productionRequiresPayment() {
        SalesDocument row = row(); when(mapper.selectOne(any(), eq(false))).thenReturn(row);
        platformLogin();
        assertThatThrownBy(() -> service.startProduction(1L)).isInstanceOf(ServiceException.class);
    }

    @Test
    void concurrentPaymentUpdateDoesNotRecordEvent() {
        SalesDocument row = row(); when(mapper.selectOne(any(), eq(false))).thenReturn(row);
        when(mapper.update(isNull(), any())).thenReturn(0);
        SalesPaymentBo bo = new SalesPaymentBo(); bo.setPaymentMethod("BANK_TRANSFER");
        bo.setPaidAmount(new BigDecimal("100"));
        platformLogin();

        assertThatThrownBy(() -> service.confirmPayment(1L, bo)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(events);
    }

    @Test
    void lifecycleActionsUseOneDocumentLock() {
        Set<String> actions = Set.of("quote", "reopen", "submit", "cancel", "confirmPayment",
            "startProduction", "completeProduction", "ship", "deliver");
        for (var method : SalesDocumentLifecycleServiceImpl.class.getDeclaredMethods()) {
            if (!actions.contains(method.getName())) continue;
            Lock4j lock = method.getAnnotation(Lock4j.class);
            assertThat(lock).as(method.getName()).isNotNull();
            assertThat(lock.name()).as(method.getName()).isEqualTo("sales-document-lifecycle");
        }
    }

    private SalesDocument row() {
        SalesDocument row = new SalesDocument(); row.setSalesDocumentId(1L); row.setTenantId(300001L);
        row.setDocumentStatus("SUBMITTED"); row.setPaymentStatus("UNPAID"); row.setProductionStatus("PENDING");
        row.setShipmentStatus("UNSHIPPED"); row.setTotalAmount(new BigDecimal("100")); row.setDelFlag("0");
        return row;
    }

    private void platformLogin() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 1L, "admin");
    }
}
