package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.mapper.MerchantCreditAccountMapper;
import com.bocoo.pay.service.PayOperatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditAccountRepositoryTest extends PayServiceTestSupport {
    @Mock MerchantCreditAccountMapper mapper;
    @Mock PayOperatorContext operator;

    @Test
    void internalSubjectUsesSalesStoreAndCurrencyAsIdentity() {
        when(mapper.selectOne(any(), eq(false))).thenAnswer(invocation -> {
            LambdaQueryWrapper<?> query = invocation.getArgument(0);
            assertThat(query.getSqlSegment()).contains("business_origin", "currency", "sales_store_id");
            return null;
        });
        CreditAccountRepository repository = new CreditAccountRepository(mapper, operator);

        repository.find(CreditSubject.internal(1L, 88L, "West store", "USD"));
    }

    @Test
    void concurrentCreateReturnsAccountInsertedByOtherRequest() {
        CreditSubject subject = CreditSubject.merchant(300001L, 10L, "Merchant", "USD");
        MerchantCreditAccount existing = new MerchantCreditAccount();
        existing.setCreditAccountId(99L);
        when(mapper.selectOne(any(), eq(false))).thenReturn(null, existing);
        when(mapper.insertIgnoreConflict(any())).thenReturn(0);
        CreditOccupyBo bo = new CreditOccupyBo();
        bo.setConfiguredCreditLimit(new BigDecimal("1000.00"));
        CreditAccountRepository repository = new CreditAccountRepository(mapper, operator);

        assertThat(repository.getOrCreate(subject, bo)).isSameAs(existing);

        verify(mapper).insertIgnoreConflict(any());
    }
}
