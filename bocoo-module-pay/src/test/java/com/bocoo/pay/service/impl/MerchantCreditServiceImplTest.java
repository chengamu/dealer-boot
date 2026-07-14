package com.bocoo.pay.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.bo.CreditRepayBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.entity.MerchantCreditTransaction;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayChannelService;
import com.bocoo.pay.service.PayOperatorContext;
import com.bocoo.pay.service.PaymentSuccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantCreditServiceImplTest extends PayServiceTestSupport {
    @Mock CreditAccountRepository accounts;
    @Mock CreditLedgerWriter ledger;
    @Mock MerchantReceivableMapper receivableMapper;
    @Mock PayOrderMapper orderMapper;
    @Mock PayOrderExtensionMapper extensionMapper;
    @Mock PayChannelService channelService;
    @Mock PaymentSuccessService successService;
    @Mock PayOperatorContext operator;
    @Mock CreditSubjectSupport subjects;
    MerchantCreditServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MerchantCreditServiceImpl(accounts, ledger, receivableMapper, orderMapper,
            extensionMapper, channelService, successService, operator, subjects);
    }

    @Test
    void occupyAtomicallyCreatesReceivableAndConfirmsPayment() {
        PayOrder order = order();
        MerchantCreditAccount account = account("1000.00", "100.00");
        PayChannel channel = new PayChannel();
        channel.setId(8L);
        channel.setCode("credit_limit");
        MerchantReceivable receivable = new MerchantReceivable();
        receivable.setReceivableNo("AR-1");
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(operator.tenantId()).thenReturn(300001L);
        CreditSubject subject = CreditSubject.merchant(300001L, 100L, "Merchant", "USD");
        when(subjects.forDocument(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq("USD"), any()))
            .thenReturn(subject);
        when(accounts.getOrCreate(org.mockito.ArgumentMatchers.eq(subject), any())).thenReturn(account);
        when(receivableMapper.selectCount(any())).thenReturn(0L);
        when(channelService.getEnabledChannel(1L, 7L, "credit_limit")).thenReturn(channel);
        when(extensionMapper.insert(any())).thenAnswer(invocation -> {
            ((PayOrderExtension) invocation.getArgument(0)).setId(22L);
            return 1;
        });
        when(orderMapper.update(any(), any())).thenReturn(1);
        when(ledger.createReceivable(account, order, 10L, 30)).thenReturn(receivable);

        MerchantReceivable result = service.occupy(command());

        assertThat(result.getReceivableNo()).isEqualTo("AR-1");
        verify(accounts).changeUsed(account, new BigDecimal("978.40"));
        verify(successService).confirm(any(), any(), org.mockito.ArgumentMatchers.eq(87840L),
            org.mockito.ArgumentMatchers.eq("USD"), any(), any());
    }

    @Test
    void occupyRejectsInsufficientCreditBeforeWritingAttempt() {
        PayOrder order = order();
        MerchantCreditAccount account = account("500.00", "100.00");
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(operator.tenantId()).thenReturn(300001L);
        CreditSubject subject = CreditSubject.merchant(300001L, 100L, "Merchant", "USD");
        when(subjects.forDocument(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq("USD"), any()))
            .thenReturn(subject);
        when(accounts.getOrCreate(org.mockito.ArgumentMatchers.eq(subject), any())).thenReturn(account);
        when(receivableMapper.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> service.occupy(command()))
            .isInstanceOf(ServiceException.class).hasMessageContaining("insufficient");
        verify(extensionMapper, never()).insert(any());
    }

    @Test
    void platformAdjustmentRunsInsideExplicitCrossTenantScope() {
        MerchantCreditAccount account = account("1000.00", "100.00");
        CreditAdjustBo bo = new CreditAdjustBo();
        bo.setAmount(new BigDecimal("250.00"));
        bo.setReason("Approved limit increase");
        when(operator.isPlatform()).thenReturn(true);
        when(accounts.byId(30L)).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return account;
        });

        MerchantCreditAccount result = service.adjust(30L, bo);

        assertThat(result.getCreditLimit()).isEqualByComparingTo("1250.00");
        verify(accounts).changeLimit(account, new BigDecimal("1250.00"));
    }

    @Test
    void partialRepaymentUsesExactAmountAndWritesIdempotencyFacts() {
        MerchantCreditAccount account = account("1000.00", "500.00");
        MerchantReceivable receivable = receivable("500.00", "0.00", "500.00", "UNPAID");
        CreditRepayBo bo = repayment("125.25", "repay-1");
        when(operator.isPlatform()).thenReturn(true);
        when(receivableMapper.selectById(40L)).thenReturn(receivable);
        when(accounts.byId(30L)).thenReturn(account);
        when(receivableMapper.update(any(), any())).thenReturn(1);

        MerchantReceivable result = service.repay(40L, bo);

        assertThat(result.getRepaidAmount()).isEqualByComparingTo("125.25");
        assertThat(result.getOutstandingAmount()).isEqualByComparingTo("374.75");
        assertThat(result.getStatus()).isEqualTo("PARTIALLY_PAID");
        verify(accounts).changeUsed(account, new BigDecimal("374.75"));
        verify(ledger).repayment(account, receivable, bo, new BigDecimal("500.00"), new BigDecimal("374.75"));
    }

    @Test
    void repeatedRepaymentReturnsCurrentFactWithoutChangingBalance() {
        MerchantCreditTransaction prior = new MerchantCreditTransaction();
        prior.setAmount(new BigDecimal("125.25"));
        MerchantReceivable current = receivable("500.00", "125.25", "374.75", "PARTIALLY_PAID");
        CreditRepayBo bo = repayment("125.25", "repay-1");
        when(operator.isPlatform()).thenReturn(true);
        when(ledger.findRepayment(40L, "repay-1")).thenReturn(prior);
        when(receivableMapper.selectById(40L)).thenReturn(current);

        assertThat(service.repay(40L, bo)).isSameAs(current);
        verify(accounts, never()).changeUsed(any(), any());
        verify(receivableMapper, never()).update(any(), any());
    }

    private CreditOccupyBo command() {
        CreditOccupyBo bo = new CreditOccupyBo();
        bo.setPayOrderId(1L);
        bo.setSalesDocumentId(10L);
        bo.setMerchantId(100L);
        bo.setMerchantName("Merchant");
        bo.setConfiguredCreditLimit(new BigDecimal("1000.00"));
        bo.setCreditTermDays(30);
        return bo;
    }

    private PayOrder order() {
        PayOrder row = new PayOrder();
        row.setId(1L);
        row.setNo("PO-1");
        row.setTenantId(300001L);
        row.setPayerTenantId(300001L);
        row.setPayeeTenantId(1L);
        row.setAppId(7L);
        row.setPrice(87840L);
        row.setCurrency("USD");
        row.setStatus(PayOrderStatus.WAITING.getStatus());
        return row;
    }

    private MerchantCreditAccount account(String limit, String used) {
        MerchantCreditAccount row = new MerchantCreditAccount();
        row.setCreditAccountId(30L);
        row.setBusinessOrigin("MERCHANT");
        row.setTenantId(300001L);
        row.setMerchantId(100L);
        row.setCreditLimit(new BigDecimal(limit));
        row.setUsedCredit(new BigDecimal(used));
        row.setCurrency("USD");
        row.setStatus("NORMAL");
        row.setVersion(0);
        return row;
    }

    private MerchantReceivable receivable(String amount, String repaid, String outstanding, String status) {
        MerchantReceivable row = new MerchantReceivable();
        row.setReceivableId(40L);
        row.setCreditAccountId(30L);
        row.setReceivableNo("AR-40");
        row.setReceivableAmount(new BigDecimal(amount));
        row.setRepaidAmount(new BigDecimal(repaid));
        row.setOutstandingAmount(new BigDecimal(outstanding));
        row.setStatus(status);
        return row;
    }

    private CreditRepayBo repayment(String amount, String key) {
        CreditRepayBo bo = new CreditRepayBo();
        bo.setAmount(new BigDecimal(amount));
        bo.setPaidTime(LocalDateTime.of(2026, 7, 13, 8, 0));
        bo.setMethod("BANK_TRANSFER");
        bo.setReference("REF-1");
        bo.setReason("Confirmed receipt");
        bo.setIdempotencyKey(key);
        return bo;
    }
}
