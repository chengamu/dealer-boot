package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.domain.vo.CustomerQuoteExportCnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteExportEnVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class CustomerQuoteExportAssembler {

    List<CustomerQuoteExportCnVo> cnRows(CustomerQuoteVo quote) {
        return quote.getItems().stream().map(item -> cnRow(quote, item)).toList();
    }

    List<CustomerQuoteExportEnVo> enRows(CustomerQuoteVo quote) {
        return quote.getItems().stream().map(item -> enRow(quote, item)).toList();
    }

    private CustomerQuoteExportCnVo cnRow(CustomerQuoteVo quote, CustomerQuoteItemVo item) {
        CustomerQuoteExportCnVo row = new CustomerQuoteExportCnVo();
        row.setQuoteNo(quote.getQuoteNo());
        row.setCustomerName(quote.getCustomerName());
        row.setCompanyName(quote.getCompanyName());
        row.setProjectName(quote.getProjectName());
        row.setValidUntil(quote.getValidUntil());
        row.setRoomLocation(item.getRoomLocation());
        row.setSaleProductName(item.getSaleProductName());
        row.setWidthInch(item.getOrderWidthInch());
        row.setHeightInch(item.getOrderHeightInch());
        row.setQuantity(item.getQuantity());
        row.setConfiguration(item.getSelectedOptionsSummaryCn());
        row.setUnitAmount(item.getUnitAmount());
        row.setShippingAmount(item.getShippingAmount());
        row.setLineAmount(item.getLineAmount());
        row.setQuoteTotal(quote.getTotalAmount());
        return row;
    }

    private CustomerQuoteExportEnVo enRow(CustomerQuoteVo quote, CustomerQuoteItemVo item) {
        CustomerQuoteExportEnVo row = new CustomerQuoteExportEnVo();
        row.setQuoteNo(quote.getQuoteNo());
        row.setCustomerName(quote.getCustomerName());
        row.setCompanyName(quote.getCompanyName());
        row.setProjectName(quote.getProjectName());
        row.setValidUntil(quote.getValidUntil());
        row.setRoomLocation(item.getRoomLocation());
        row.setSaleProductName(item.getSaleProductName());
        row.setWidthInch(item.getOrderWidthInch());
        row.setHeightInch(item.getOrderHeightInch());
        row.setQuantity(item.getQuantity());
        row.setConfiguration(item.getSelectedOptionsSummaryEn());
        row.setUnitAmount(item.getUnitAmount());
        row.setShippingAmount(item.getShippingAmount());
        row.setLineAmount(item.getLineAmount());
        row.setQuoteTotal(quote.getTotalAmount());
        return row;
    }
}
