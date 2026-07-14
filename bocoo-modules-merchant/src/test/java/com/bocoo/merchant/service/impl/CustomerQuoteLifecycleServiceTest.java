package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.merchant.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
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
    @Mock
    private CustomerQuotePricingSessionFactory sessionFactory;
    @Mock
    private CustomerQuoteCatalogService catalogService;
    private CustomerQuotePricingSession session;
    @Mock
    private SalesOwnershipResolver ownershipResolver;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 200L, 7L, "sales");
        session = new CustomerQuotePricingSession(200L, catalogService);
        org.mockito.Mockito.lenient().when(ownershipResolver.currentBusinessOrigin()).thenReturn("MERCHANT");
    }

    @Test
    void confirmRecalculatesAndFreezesDraft() {
        CustomerQuote quote = quote("DRAFT", "ZH_CN");
        CustomerQuoteItemBo row = row();
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(itemWriter.currentRows(1L, 200L)).thenReturn(List.of(row));
        when(sessionFactory.create(200L)).thenReturn(session);
        when(itemWriter.replace(1L, 200L, List.of(row), session)).thenReturn(result(true));
        when(quoteMapper.updateById(any())).thenReturn(1);
        when(quoteMapper.update(any(), any())).thenReturn(1);

        CustomerQuoteLifecycleServiceImpl service = service();
        assertThat(service.confirm(200L, 1L)).isTrue();

        assertThat(quote.getStatus()).isEqualTo("CONFIRMED");
        assertThat(quote.getTotalAmount()).isEqualByComparingTo("230.00");
        assertThat(quote.getConfirmedBy()).isEqualTo("sales");
        verify(itemWriter).replace(1L, 200L, List.of(row), session);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<CustomerQuote>> queryCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(quoteMapper).selectOne(queryCaptor.capture(), eq(false));
        assertThat(queryCaptor.getValue().getSqlSegment()).contains("tenant_id", "business_origin");
        assertThat(queryCaptor.getValue().getParamNameValuePairs().values()).contains(200L, "MERCHANT");
    }

    @Test
    void confirmBlocksIncompleteEnglishLabels() {
        CustomerQuote quote = quote("DRAFT", "EN_US");
        CustomerQuoteItemBo row = row();
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(itemWriter.currentRows(1L, 200L)).thenReturn(List.of(row));
        when(sessionFactory.create(200L)).thenReturn(session);
        when(itemWriter.replace(1L, 200L, List.of(row), session)).thenReturn(result(true));
        when(quoteMapper.updateById(any())).thenReturn(1);
        when(calculator.optionSnapshot(10L, row.getSelectedOptionValues(), session)).thenReturn(
            new CustomerQuoteOptionSnapshot(row.getSelectedOptionValues(), "系统：电机", "SYSTEM=MOTOR", false, true));

        CustomerQuoteLifecycleServiceImpl service = service();
        assertThatThrownBy(() -> service.confirm(200L, 1L)).isInstanceOf(ServiceException.class);

        assertThat(quote.getStatus()).isEqualTo("DRAFT");
        verify(quoteMapper, never()).update(any(), any());
    }

    @Test
    void confirmRejectsConcurrentStatusChange() {
        CustomerQuote quote = quote("DRAFT", "ZH_CN");
        CustomerQuoteItemBo row = row();
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(itemWriter.currentRows(1L, 200L)).thenReturn(List.of(row));
        when(sessionFactory.create(200L)).thenReturn(session);
        when(itemWriter.replace(1L, 200L, List.of(row), session)).thenReturn(result(true));
        when(quoteMapper.updateById(any())).thenReturn(1);
        when(quoteMapper.update(any(), any())).thenReturn(0);

        CustomerQuoteLifecycleServiceImpl service = service();

        assertThatThrownBy(() -> service.confirm(200L, 1L)).isInstanceOf(ServiceException.class);
        assertThat(quote.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void repeatedConfirmIsIdempotent() {
        CustomerQuote quote = quote("CONFIRMED", "ZH_CN");
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);

        assertThat(service().confirm(200L, 1L)).isTrue();

        verify(itemWriter, never()).currentRows(any(), any());
        verify(quoteMapper, never()).update(any(), any());
    }

    @Test
    void voidQuoteOnlyChangesConfirmedQuoteToVoid() {
        CustomerQuote quote = quote("CONFIRMED", "EN_US");
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        when(quoteMapper.update(any(), any())).thenReturn(1);

        CustomerQuoteLifecycleServiceImpl service = service();

        assertThat(service.voidQuote(1L)).isTrue();
        assertThat(quote.getStatus()).isEqualTo("VOID");
        verify(quoteMapper).update(any(), any());
    }

    @Test
    void convertedQuoteCannotBeVoided() {
        CustomerQuote quote = quote("CONFIRMED", "EN_US");
        quote.setSalesDocumentId(99L);
        when(quoteMapper.selectOne(any(), eq(false))).thenReturn(quote);
        CustomerQuoteLifecycleServiceImpl service = service();

        assertThatThrownBy(() -> service.voidQuote(1L)).isInstanceOf(ServiceException.class);
        verify(quoteMapper, never()).update(any(), any());
    }

    private CustomerQuote quote(String status, String language) {
        CustomerQuote quote = new CustomerQuote();
        quote.setQuoteId(1L);
        quote.setTenantId(200L);
        quote.setBusinessOrigin("MERCHANT");
        quote.setDeptId(100L);
        quote.setOwnerUserId(7L);
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

    private CustomerQuoteWriteResult result(boolean allPassed) {
        return new CustomerQuoteWriteResult(
            new CustomerQuoteTotals("USD", amount("200"), amount("30"), amount("230"), allPassed), List.of());
    }

    private CustomerQuoteLifecycleServiceImpl service() {
        return new CustomerQuoteLifecycleServiceImpl(
            quoteMapper, itemWriter, calculator, sessionFactory, ownershipResolver);
    }

}
