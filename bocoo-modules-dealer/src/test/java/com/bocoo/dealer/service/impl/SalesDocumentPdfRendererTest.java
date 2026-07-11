package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesDocumentPdfRendererTest {
    private final SalesDocumentPdfRenderer renderer = new SalesDocumentPdfRenderer();

    @Test
    void quotePdfRequiresQuotedDocument() {
        SalesDocumentVo row = sample();
        row.setDocumentStatus("DRAFT");
        assertThatThrownBy(() -> renderer.render(row, "QUOTE")).isInstanceOf(ServiceException.class);
    }

    @Test
    void orderPdfRequiresOrderNumber() {
        SalesDocumentVo row = sample();
        row.setOrderNo(null);
        assertThatThrownBy(() -> renderer.render(row, "ORDER")).isInstanceOf(ServiceException.class);
    }

    @Test
    void rendersQuoteOrderAndProductionDocuments() {
        SalesDocumentVo row = sample();
        assertPdf(renderer.render(row, "QUOTE"));
        assertPdf(renderer.render(row, "ORDER"));
        assertPdf(renderer.render(row, "PRODUCTION"));
        assertPdf(renderer.renderProduction(row, List.of(new ProductionMaterialRow(
            "Living Room", "Zebra Shade", "M001", "Fabric", "Fabric", "m2", new BigDecimal("2.28"), "Front"))));
    }

    private SalesDocumentVo sample() {
        SalesDocumentItemVo item = new SalesDocumentItemVo();
        item.setLineNo(1); item.setRoomLocation("Living Room"); item.setSaleProductName("Zebra Shade");
        item.setOrderWidthInch(new BigDecimal("48")); item.setOrderHeightInch(new BigDecimal("72"));
        item.setQuantity(2); item.setConfigurationSummary("FABRIC=WHITE"); item.setFormulaVersionLabel("V1");
        item.setUnitAmount(new BigDecimal("120")); item.setLineAmount(new BigDecimal("250"));
        SalesDocumentVo row = new SalesDocumentVo();
        row.setQuoteNo("QT-1"); row.setOrderNo("SO-1"); row.setDocumentStatus("SUBMITTED");
        row.setCustomerName("明景窗饰"); row.setMerchantName("Demo Merchant"); row.setItems(List.of(item));
        row.setListAmount(new BigDecimal("260")); row.setDiscountAmount(new BigDecimal("10"));
        row.setShippingAmount(new BigDecimal("10")); row.setTaxAmount(BigDecimal.ZERO); row.setTotalAmount(new BigDecimal("260"));
        return row;
    }

    private void assertPdf(byte[] bytes) {
        assertThat(bytes.length).isGreaterThan(100);
        assertThat(new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
