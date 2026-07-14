package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.mapper.SalesDocumentMapper;
import com.bocoo.dealer.payment.SalesPaymentOrderLinker;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderSubmitBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.mapper.QuickOrderMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuickOrderSubmissionServiceTest {
    @Mock private QuickOrderMapper mapper;
    @Mock private SalesDocumentMapper documentMapper;
    @Mock private SalesDocumentItemMapper documentItemMapper;
    @Mock private QuickOrderAccess access;
    @Mock private QuickOrderRecalculator recalculator;
    @Mock private SalesPaymentOrderLinker paymentOrderLinker;
    private QuickOrderSubmissionServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), QuickOrder.class);
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        service = new QuickOrderSubmissionServiceImpl(mapper, documentMapper, documentItemMapper,
            access, recalculator, new QuickOrderSalesDocumentFactory(), paymentOrderLinker);
    }

    @Test
    void repeatedSubmissionReturnsExistingOrderWithoutRecalculation() {
        QuickOrder source = source(); source.setSalesDocumentId(99L); source.setStatus("ORDERED");
        when(access.load(1L)).thenReturn(source);
        SalesDocument existing = new SalesDocument(); existing.setSalesDocumentId(99L);
        existing.setOrderNo("SO-99"); existing.setTotalAmount(new BigDecimal("170.00"));
        when(documentMapper.selectOne(any(), eq(false))).thenReturn(existing);

        var result = service.submit(1L, request("170.00"));

        assertThat(result.salesDocumentId()).isEqualTo(99L);
        verifyNoInteractions(recalculator, documentItemMapper);
        verify(documentMapper, never()).insert(any());
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void amountChangeStopsBeforeCreatingSalesDocument() {
        QuickOrder source = source();
        when(access.load(1L)).thenReturn(source);
        when(recalculator.recalculate(source)).thenReturn(calculation(source));

        assertThatThrownBy(() -> service.submit(1L, request("169.99")))
            .isInstanceOf(ServiceException.class);

        verify(documentMapper, never()).insert(any());
        verify(documentItemMapper, never()).insert(any());
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void successfulSubmissionFreezesQuickOrderSourceAndSnapshots() {
        QuickOrder source = source(); QuickOrderItem item = item();
        when(access.load(1L)).thenReturn(source);
        when(recalculator.recalculate(source)).thenReturn(new QuickOrderRecalculation(source, List.of(item), null));
        doAnswer(invocation -> {
            SalesDocument row = invocation.getArgument(0); row.setSalesDocumentId(88L); return 1;
        }).when(documentMapper).insert(any());
        when(documentItemMapper.insert(any())).thenReturn(1);
        when(mapper.update(isNull(), any())).thenReturn(1);

        var result = service.submit(1L, request("170.00"));

        ArgumentCaptor<SalesDocument> header = ArgumentCaptor.forClass(SalesDocument.class);
        ArgumentCaptor<SalesDocumentItem> line = ArgumentCaptor.forClass(SalesDocumentItem.class);
        verify(documentMapper).insert(header.capture()); verify(documentItemMapper).insert(line.capture());
        assertThat(header.getValue().getSourceType()).isEqualTo("QUICK_ORDER");
        assertThat(header.getValue().getBusinessOrigin()).isEqualTo("MERCHANT");
        assertThat(header.getValue().getSalesStoreId()).isNull();
        assertThat(header.getValue().getDeptId()).isEqualTo(20L);
        assertThat(header.getValue().getOwnerUserId()).isEqualTo(7L);
        assertThat(header.getValue().getSourceQuickOrderId()).isEqualTo(1L);
        assertThat(header.getValue().getSourceQuoteId()).isNull();
        assertThat(header.getValue().getDocumentStatus()).isEqualTo("SUBMITTED");
        assertThat(header.getValue().getPaymentStatus()).isEqualTo("UNPAID");
        assertThat(line.getValue().getSourceQuickOrderItemId()).isEqualTo(11L);
        assertThat(line.getValue().getBomSnapshotJson()).isEqualTo("[{\"materialCode\":\"M1\"}]");
        assertThat(line.getValue().getPricingSnapshotJson()).isEqualTo("{\"price\":1}");
        assertThat(line.getValue().getShippingSnapshotJson()).isEqualTo("{\"shipping\":1}");
        assertThat(result.salesDocumentId()).isEqualTo(88L);
        verify(paymentOrderLinker).initialize(header.getValue());
        verify(mapper).update(isNull(), any());
    }

    @Test
    void submissionUsesUnifiedQuickOrderLock() throws NoSuchMethodException {
        Lock4j lock = QuickOrderSubmissionServiceImpl.class
            .getMethod("submit", Long.class, QuickOrderSubmitBo.class).getAnnotation(Lock4j.class);
        assertThat(lock).isNotNull(); assertThat(lock.name()).isEqualTo("quick-order");
    }

    private QuickOrderRecalculation calculation(QuickOrder source) {
        return new QuickOrderRecalculation(source, List.of(item()), null);
    }

    private QuickOrder source() {
        QuickOrder row = new QuickOrder(); row.setQuickOrderId(1L); row.setTenantId(300001L);
        row.setBusinessOrigin("MERCHANT"); row.setDeptId(20L); row.setOwnerUserId(7L);
        row.setQuickOrderNo("QO-1"); row.setStatus("DRAFT"); row.setCustomerId(2L);
        row.setCustomerName("Customer"); row.setRecipientName("Buyer"); row.setRecipientPhone("555-0100");
        row.setShippingAddress("100 Main St"); row.setCurrencyCode("USD");
        row.setListAmount(new BigDecimal("200.00")); row.setDiscountAmount(new BigDecimal("40.00"));
        row.setProductAmount(new BigDecimal("160.00")); row.setShippingAmount(new BigDecimal("10.00"));
        row.setTaxAmount(new BigDecimal("0.00")); row.setTotalAmount(new BigDecimal("170.00")); return row;
    }

    private QuickOrderItem item() {
        QuickOrderItem row = new QuickOrderItem(); row.setQuickOrderItemId(11L); row.setLineNo(1);
        row.setSaleProductId(3L); row.setSaleProductCode("P1"); row.setSaleProductName("Shade");
        row.setFormulaVersionId(4L); row.setQuantity(2); row.setListUnitAmount(new BigDecimal("100.00"));
        row.setListAmount(new BigDecimal("200.00")); row.setDiscountRate(new BigDecimal("0.8000"));
        row.setUnitAmount(new BigDecimal("80.00")); row.setProductAmount(new BigDecimal("160.00"));
        row.setShippingTemplateId(5L); row.setShippingAmount(new BigDecimal("10.00"));
        row.setLineAmount(new BigDecimal("170.00")); row.setSelectedOptionsJson("{\"CONTROL\":\"MOTOR\"}");
        row.setBomSnapshotJson("[{\"materialCode\":\"M1\"}]"); row.setPricingSnapshotJson("{\"price\":1}");
        row.setShippingSnapshotJson("{\"shipping\":1}"); return row;
    }

    private QuickOrderSubmitBo request(String expected) {
        QuickOrderSubmitBo bo = new QuickOrderSubmitBo(); bo.setExpectedTotalAmount(new BigDecimal(expected)); return bo;
    }
}
