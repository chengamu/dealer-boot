package com.bocoo.dealer.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.domain.bo.CustomerQuoteConvertOrderBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderPreviewVo;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.payment.SalesPaymentOrderLinker;
import com.bocoo.dealer.service.TestSaTokenContext;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.service.CustomerQuoteConversionSnapshot;
import com.bocoo.merchant.service.CustomerQuoteConversionSupport;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteOrderServiceTest {
    @Mock private CustomerQuoteConversionSupport quoteSupport;
    @Mock private CustomerQuoteOrderCalculator calculator;
    @Mock private SalesDocumentMapper documentMapper;
    @Mock private SalesDocumentItemMapper itemMapper;
    @Mock private SalesDocumentEventRecorder events;
    @Mock private SalesPaymentOrderLinker paymentOrderLinker;
    private CustomerQuoteOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SalesDocument.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 1L, "merchant");
        service = new CustomerQuoteOrderServiceImpl(quoteSupport, calculator, new CustomerQuoteOrderFactory(),
            documentMapper, itemMapper, events, paymentOrderLinker);
    }

    @Test
    void repeatedConversionReturnsExistingOrder() {
        CustomerQuote quote = quote(); quote.setSalesDocumentId(99L);
        when(quoteSupport.load(1L)).thenReturn(new CustomerQuoteConversionSnapshot(quote, List.of()));
        SalesDocument existing = new SalesDocument(); existing.setSalesDocumentId(99L);
        existing.setOrderNo("SO-99"); existing.setTotalAmount(new BigDecimal("190"));
        when(documentMapper.selectOne(any(), eq(false))).thenReturn(existing);

        var result = service.convert(1L, request());

        assertThat(result.getSalesDocumentId()).isEqualTo(99L);
        assertThat(result.getOrderNo()).isEqualTo("SO-99");
        verifyNoInteractions(calculator, itemMapper, events);
        verify(quoteSupport, never()).markConverted(any(), any(), any());
    }

    @Test
    void conversionCopiesFrozenSnapshotAndMarksQuote() {
        CustomerQuote quote = quote(); CustomerQuoteItem source = item();
        when(quoteSupport.load(1L)).thenReturn(new CustomerQuoteConversionSnapshot(quote, List.of(source)));
        CustomerQuoteOrderPreviewVo preview = new CustomerQuoteOrderPreviewVo();
        preview.setCurrencyCode("USD"); preview.setListAmount(new BigDecimal("200"));
        preview.setDiscountAmount(new BigDecimal("40")); preview.setProductAmount(new BigDecimal("160"));
        preview.setShippingAmount(new BigDecimal("30")); preview.setTotalAmount(new BigDecimal("190"));
        var rates = new LinkedHashMap<Long, BigDecimal>(); rates.put(11L, new BigDecimal("0.80"));
        when(calculator.calculate(any())).thenReturn(new CustomerQuoteOrderCalculation(null, preview, rates));
        doAnswer(invocation -> {
            SalesDocument row = invocation.getArgument(0); row.setSalesDocumentId(88L); return 1;
        }).when(documentMapper).insert(any());
        when(itemMapper.insert(any())).thenReturn(1);
        when(quoteSupport.markConverted(eq(1L), eq(88L), any())).thenReturn(true);

        var result = service.convert(1L, request());

        ArgumentCaptor<SalesDocument> document = ArgumentCaptor.forClass(SalesDocument.class);
        ArgumentCaptor<SalesDocumentItem> line = ArgumentCaptor.forClass(SalesDocumentItem.class);
        verify(documentMapper).insert(document.capture()); verify(itemMapper).insert(line.capture());
        assertThat(document.getValue().getSourceQuoteId()).isEqualTo(1L);
        assertThat(document.getValue().getSourceType()).isEqualTo("QUOTE");
        assertThat(document.getValue().getSourceNo()).isEqualTo("QT-1");
        assertThat(document.getValue().getDocumentStatus()).isEqualTo("SUBMITTED");
        assertThat(line.getValue().getSourceQuoteItemId()).isEqualTo(11L);
        assertThat(line.getValue().getBomSnapshotJson()).isEqualTo("[{\"materialCode\":\"M1\"}]");
        assertThat(line.getValue().getShippingSnapshotJson()).isEqualTo("{\"templateId\":5}");
        assertThat(line.getValue().getProductAmount()).isEqualByComparingTo("160.00");
        assertThat(line.getValue().getShippingAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getSalesDocumentId()).isEqualTo(88L);
        verify(paymentOrderLinker).initialize(document.getValue());
        verify(events).record(eq(88L), eq(300001L), eq("ORDER_CREATED_FROM_QUOTE"),
            eq("CONFIRMED"), eq("SUBMITTED"), eq("QT-1"));
    }

    @Test
    void conversionUsesDedicatedQuoteLock() throws NoSuchMethodException {
        Lock4j lock = CustomerQuoteOrderServiceImpl.class
            .getMethod("convert", Long.class, CustomerQuoteConvertOrderBo.class).getAnnotation(Lock4j.class);
        assertThat(lock).isNotNull(); assertThat(lock.name()).isEqualTo("customer-quote-convert");
    }

    private CustomerQuote quote() {
        CustomerQuote row = new CustomerQuote(); row.setQuoteId(1L); row.setTenantId(300001L);
        row.setQuoteNo("QT-1"); row.setStatus("CONFIRMED"); row.setQuoteLanguage("EN_US");
        row.setCustomerId(2L); row.setCustomerName("Customer"); row.setCurrencyCode("USD");
        return row;
    }

    private CustomerQuoteItem item() {
        CustomerQuoteItem row = new CustomerQuoteItem(); row.setQuoteItemId(11L); row.setLineNo(1);
        row.setSaleProductId(3L); row.setSaleProductCode("P1"); row.setSaleProductName("Shade");
        row.setFormulaVersionId(4L); row.setQuantity(2); row.setUnitAmount(new BigDecimal("100"));
        row.setProductAmount(new BigDecimal("200")); row.setShippingTemplateId(5L);
        row.setShippingAmount(new BigDecimal("30")); row.setBomSnapshotJson("[{\"materialCode\":\"M1\"}]");
        row.setPricingSnapshotJson("{\"priceSettingId\":6}");
        row.setShippingSnapshotJson("{\"templateId\":5}"); return row;
    }

    private CustomerQuoteConvertOrderBo request() {
        CustomerQuoteConvertOrderBo bo = new CustomerQuoteConvertOrderBo();
        bo.setRecipientName("Buyer"); bo.setRecipientPhone("555-0100");
        bo.setShippingAddress("100 Main St"); bo.setExpectedTotalAmount(new BigDecimal("190")); return bo;
    }
}
