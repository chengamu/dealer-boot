package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.dealer.domain.bo.CustomerQuoteConvertOrderBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
class CustomerQuoteOrderFactory {
    SalesDocument header(CustomerQuote quote, CustomerQuoteOrderCalculation calculation,
                         CustomerQuoteConvertOrderBo bo) {
        SalesDocument row = new SalesDocument();
        row.setTenantId(quote.getTenantId());
        row.setMerchantId(calculation.profile() == null ? null : calculation.profile().getMerchantId());
        row.setMerchantName(calculation.profile() == null ? null : calculation.profile().getMerchantName());
        row.setSourceType("QUOTE"); row.setSourceQuoteId(quote.getQuoteId());
        row.setSourceNo(quote.getQuoteNo()); row.setQuoteNo(quote.getQuoteNo());
        row.setOrderNo("SO-" + Seq.getId()); row.setCustomerId(quote.getCustomerId());
        row.setCustomerName(quote.getCustomerName()); row.setCompanyName(quote.getCompanyName());
        row.setCustomerEmail(quote.getCustomerEmail()); row.setCustomerPhone(quote.getCustomerPhone());
        row.setOwnerUserId(quote.getOwnerUserId()); row.setOwnerName(quote.getOwnerName());
        row.setProjectName(quote.getProjectName()); row.setCustomerPoNo(bo.getCustomerPoNo());
        row.setValidUntil(quote.getValidUntil()); row.setRecipientName(bo.getRecipientName());
        row.setRecipientPhone(bo.getRecipientPhone()); row.setShippingAddress(bo.getShippingAddress());
        var totals = calculation.preview(); row.setCurrencyCode(totals.getCurrencyCode());
        row.setListAmount(totals.getListAmount()); row.setDiscountAmount(totals.getDiscountAmount());
        row.setProductAmount(totals.getProductAmount()); row.setShippingAmount(totals.getShippingAmount());
        row.setTaxAmount(BigDecimal.ZERO.setScale(2)); row.setTotalAmount(totals.getTotalAmount());
        row.setDiscountRate(weightedRate(totals.getProductAmount(), totals.getListAmount()));
        row.setDocumentStatus("SUBMITTED"); row.setPaymentStatus("UNPAID");
        row.setProductionStatus("PENDING"); row.setShipmentStatus("UNSHIPPED");
        row.setSubmittedTime(TimeUtils.utcNow()); row.setDelFlag("0"); row.setRemark(bo.getRemark());
        return row;
    }

    SalesDocumentItem item(CustomerQuote quote, CustomerQuoteItem source, BigDecimal rate) {
        SalesDocumentItem row = new SalesDocumentItem();
        row.setSourceQuoteItemId(source.getQuoteItemId()); row.setLineNo(source.getLineNo());
        row.setRoomLocation(source.getRoomLocation()); row.setSaleProductId(source.getSaleProductId());
        row.setSaleProductCode(source.getSaleProductCode()); row.setSaleProductName(source.getSaleProductName());
        row.setCategoryId(source.getCategoryId()); row.setCategoryCode(source.getCategoryCode());
        row.setCategoryNameCn(source.getCategoryNameCn()); row.setProductTypeCode(source.getProductTypeCode());
        row.setProductTypeNameCn(source.getProductTypeNameCn()); row.setFormulaId(source.getFormulaId());
        row.setFormulaVersionId(source.getFormulaVersionId()); row.setFormulaVersionLabel(source.getFormulaVersionLabel());
        row.setOrderWidthInch(source.getOrderWidthInch()); row.setOrderHeightInch(source.getOrderHeightInch());
        row.setQuantity(source.getQuantity()); row.setSelectedOptionsJson(source.getSelectedOptionsJson());
        row.setConfigurationSummary("EN_US".equals(quote.getQuoteLanguage())
            ? source.getSelectedOptionsSummaryEn() : source.getSelectedOptionsSummaryCn());
        row.setConfigurationSummaryCn(source.getSelectedOptionsSummaryCn());
        row.setConfigurationSummaryEn(source.getSelectedOptionsSummaryEn());
        row.setCalculationStatus("PASS"); row.setListUnitAmount(money(source.getUnitAmount()));
        row.setListAmount(money(source.getProductAmount())); row.setDiscountRate(rate);
        row.setProductAmount(money(source.getProductAmount().multiply(rate)));
        row.setDiscountAmount(money(row.getListAmount().subtract(row.getProductAmount())));
        row.setUnitAmount(money(row.getProductAmount().divide(BigDecimal.valueOf(source.getQuantity()), 2, RoundingMode.HALF_UP)));
        row.setShippingTemplateId(source.getShippingTemplateId());
        row.setUnitShippingAmount(money(source.getUnitShippingAmount()));
        row.setShippingAmount(money(source.getShippingAmount()));
        row.setLineAmount(money(row.getProductAmount().add(row.getShippingAmount())));
        row.setBomSnapshotJson(source.getBomSnapshotJson()); row.setPricingSnapshotJson(source.getPricingSnapshotJson());
        row.setShippingSnapshotJson(source.getShippingSnapshotJson()); row.setSortOrder(source.getSortOrder());
        row.setDelFlag("0"); row.setRemark(source.getRemark());
        return row;
    }

    private BigDecimal weightedRate(BigDecimal product, BigDecimal list) {
        return list == null || list.signum() == 0 ? BigDecimal.ONE : product.divide(list, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
