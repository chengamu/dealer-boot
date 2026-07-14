package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteDraftCopyTest {

    @Mock
    private CustomerQuoteMapper quoteMapper;
    @Mock
    private CustomerQuoteItemMapper itemMapper;
    @Mock
    private CustomerQuoteHeaderNormalizer headerNormalizer;
    @Mock
    private CustomerQuoteItemWriter itemWriter;
    @Mock
    private CustomerQuoteCalculator calculator;
    @Mock
    private CustomerQuoteDraftAssembler draftAssembler;
    @Mock
    private CustomerQuotePricingSessionFactory sessionFactory;
    @Mock
    private SalesOwnershipResolver ownershipResolver;

    @Test
    void copyResolvesNewOwnershipInsteadOfSubmittingSourceOwner() {
        CustomerQuote source = new CustomerQuote();
        source.setQuoteId(1L);
        source.setTenantId(300L);
        source.setBusinessOrigin("MERCHANT");
        source.setCustomerId(10L);
        source.setProjectName("Project");
        source.setOwnerUserId(99L);
        when(ownershipResolver.currentBusinessOrigin()).thenReturn("MERCHANT");
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(source);
        when(itemWriter.currentRows(1L, 300L)).thenReturn(List.of());
        CustomerQuote copy = new CustomerQuote();
        copy.setQuoteId(2L);
        copy.setTenantId(300L);
        when(headerNormalizer.newQuote(any())).thenReturn(copy);
        when(itemWriter.replace(eq(2L), eq(300L), any())).thenReturn(writeResult());
        CustomerQuoteVo result = new CustomerQuoteVo();
        result.setQuoteId(2L);
        when(draftAssembler.assemble(eq(copy), any())).thenReturn(result);

        Long copyId = service().copy(1L);

        assertThat(copyId).isEqualTo(2L);
        ArgumentCaptor<CustomerQuoteBo> captor = ArgumentCaptor.forClass(CustomerQuoteBo.class);
        verify(headerNormalizer).newQuote(captor.capture());
        assertThat(captor.getValue().getOwnerUserId()).isNull();
    }

    private CustomerQuoteWriteResult writeResult() {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        return new CustomerQuoteWriteResult(
            new CustomerQuoteTotals("USD", zero, zero, zero, true), List.of());
    }

    private CustomerQuoteDraftServiceImpl service() {
        return new CustomerQuoteDraftServiceImpl(quoteMapper, itemMapper, headerNormalizer, itemWriter,
            calculator, draftAssembler, sessionFactory, ownershipResolver);
    }
}
