package com.bocoo.pay.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.pay.domain.bo.CreditTransactionQueryBo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.mapper.MerchantCreditTransactionMapper;
import com.bocoo.pay.mapper.MerchantReceivableMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessCreditQueryServiceImplTest extends PayServiceTestSupport {
    @Mock
    private CreditSubjectSupport subjects;
    @Mock
    private CreditAccountRepository accounts;
    @Mock
    private MerchantCreditTransactionMapper transactionMapper;
    @Mock
    private MerchantReceivableMapper receivableMapper;

    private BusinessCreditQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        CreditSubject subject = CreditSubject.merchant(300001L, 400001L, "New Merchant", "USD");
        when(subjects.current("USD")).thenReturn(subject);
        when(accounts.find(subject)).thenReturn(null);
        service = new BusinessCreditQueryServiceImpl(subjects, accounts, transactionMapper, receivableMapper);
    }

    @Test
    void accountReturnsEmptyStateWhenCreditIsNotAllocated() {
        assertThat(service.account()).isNull();
    }

    @Test
    void transactionsReturnEmptyPageWhenCreditIsNotAllocated() {
        var result = service.transactions(new CreditTransactionQueryBo(), new PageQuery());

        assertThat(result.getRows()).isEmpty();
        assertThat(result.getTotal()).isZero();
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void receivablesReturnEmptyPageWhenCreditIsNotAllocated() {
        var result = service.receivables(new ReceivableQueryBo(), new PageQuery());

        assertThat(result.getRows()).isEmpty();
        assertThat(result.getTotal()).isZero();
        verifyNoInteractions(receivableMapper);
    }
}
