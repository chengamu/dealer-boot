package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteLifecycleServiceTest {

    @Mock
    private CustomerQuoteMapper quoteMapper;
    @Mock
    private CustomerQuoteItemWriter itemWriter;
    @Mock
    private CustomerQuoteCalculator calculator;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 200L, 7L, "sales");
    }

    @Test
    void confirmRecalculatesAndFreezesDraft() {
        CustomerQuote quote = quote("DRAFT", "ZH_CN");
        CustomerQuoteItemBo row = row();
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(itemWriter.currentRows(1L, 200L)).thenReturn(List.of(row));
        when(itemWriter.replace(1L, 200L, List.of(row))).thenReturn(
            new CustomerQuoteTotals("USD", amount("200"), amount("30"), amount("230"), true));
        when(quoteMapper.updateById(any())).thenReturn(1);
        when(quoteMapper.update(any(), any())).thenReturn(1);

        CustomerQuoteLifecycleServiceImpl service = new CustomerQuoteLifecycleServiceImpl(quoteMapper, itemWriter, calculator);
        assertThat(service.confirm(1L)).isTrue();

        assertThat(quote.getStatus()).isEqualTo("CONFIRMED");
        assertThat(quote.getTotalAmount()).isEqualByComparingTo("230.00");
        assertThat(quote.getConfirmedBy()).isEqualTo("sales");
        verify(itemWriter).replace(1L, 200L, List.of(row));
    }

    @Test
    void confirmBlocksIncompleteEnglishLabels() {
        CustomerQuote quote = quote("DRAFT", "EN_US");
        CustomerQuoteItemBo row = row();
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(itemWriter.currentRows(1L, 200L)).thenReturn(List.of(row));
        when(itemWriter.replace(1L, 200L, List.of(row))).thenReturn(
            new CustomerQuoteTotals("USD", amount("200"), amount("30"), amount("230"), true));
        when(quoteMapper.updateById(any())).thenReturn(1);
        when(calculator.optionSnapshot(10L, row.getSelectedOptionValues())).thenReturn(
            new CustomerQuoteOptionSnapshot(row.getSelectedOptionValues(), "系统：电机", "SYSTEM=MOTOR", false, true));

        CustomerQuoteLifecycleServiceImpl service = new CustomerQuoteLifecycleServiceImpl(quoteMapper, itemWriter, calculator);
        assertThatThrownBy(() -> service.confirm(1L)).isInstanceOf(ServiceException.class);

        assertThat(quote.getStatus()).isEqualTo("DRAFT");
        verify(quoteMapper, never()).update(any(), any());
    }

    @Test
    void confirmRejectsConcurrentStatusChange() {
        CustomerQuote quote = quote("DRAFT", "ZH_CN");
        CustomerQuoteItemBo row = row();
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(itemWriter.currentRows(1L, 200L)).thenReturn(List.of(row));
        when(itemWriter.replace(1L, 200L, List.of(row))).thenReturn(
            new CustomerQuoteTotals("USD", amount("200"), amount("30"), amount("230"), true));
        when(quoteMapper.updateById(any())).thenReturn(1);
        when(quoteMapper.update(any(), any())).thenReturn(0);

        CustomerQuoteLifecycleServiceImpl service = new CustomerQuoteLifecycleServiceImpl(quoteMapper, itemWriter, calculator);

        assertThatThrownBy(() -> service.confirm(1L)).isInstanceOf(ServiceException.class);
        assertThat(quote.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void voidQuoteOnlyChangesConfirmedQuoteToVoid() {
        CustomerQuote quote = quote("CONFIRMED", "EN_US");
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(quoteMapper.update(any(), any())).thenReturn(1);

        CustomerQuoteLifecycleServiceImpl service = new CustomerQuoteLifecycleServiceImpl(quoteMapper, itemWriter, calculator);

        assertThat(service.voidQuote(1L)).isTrue();
        assertThat(quote.getStatus()).isEqualTo("VOID");
        verify(quoteMapper).update(any(), any());
    }

    private CustomerQuote quote(String status, String language) {
        CustomerQuote quote = new CustomerQuote();
        quote.setQuoteId(1L);
        quote.setTenantId(200L);
        quote.setStatus(status);
        quote.setQuoteLanguage(language);
        quote.setDelFlag("0");
        return quote;
    }

    private CustomerQuoteItemBo row() {
        CustomerQuoteItemBo row = new CustomerQuoteItemBo();
        row.setSaleProductId(10L);
        row.setSelectedOptionValues(Map.of("SYSTEM", "MOTOR"));
        return row;
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value).setScale(2);
    }

}
