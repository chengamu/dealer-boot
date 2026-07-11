package com.bocoo.dealer.service.impl;

import com.bocoo.dealer.domain.vo.SalesDocumentExportVo;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class SalesDocumentExportAssembler {
    List<SalesDocumentExportVo> rows(SalesDocumentVo document) {
        return document.getItems().stream().map(item -> row(document, item)).toList();
    }

    private SalesDocumentExportVo row(SalesDocumentVo document, SalesDocumentItemVo item) {
        SalesDocumentExportVo row = new SalesDocumentExportVo();
        row.setDocumentNo(document.getOrderNo() == null ? document.getQuoteNo() : document.getOrderNo());
        row.setCustomerName(document.getCustomerName());
        row.setCompanyName(document.getCompanyName());
        row.setProjectName(document.getProjectName());
        row.setRoomLocation(item.getRoomLocation());
        row.setSaleProductName(item.getSaleProductName());
        row.setWidthInch(item.getOrderWidthInch());
        row.setHeightInch(item.getOrderHeightInch());
        row.setQuantity(item.getQuantity());
        row.setConfiguration(item.getConfigurationSummary());
        row.setUnitAmount(item.getUnitAmount());
        row.setShippingAmount(item.getShippingAmount());
        row.setLineAmount(item.getLineAmount());
        row.setDocumentTotal(document.getTotalAmount());
        return row;
    }
}
