package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
class QuickOrderSalesDocumentFactory {
    SalesDocument header(QuickOrder source, MerchantProfileVo profile) {
        SalesDocument row = new SalesDocument();
        row.setTenantId(source.getTenantId());
        row.setMerchantId(profile == null ? null : profile.getMerchantId());
        row.setMerchantName(profile == null ? null : profile.getMerchantName());
        row.setSourceType("QUICK_ORDER"); row.setSourceQuickOrderId(source.getQuickOrderId());
        row.setSourceNo(source.getQuickOrderNo()); row.setOrderNo("SO-" + Seq.getId());
        row.setCustomerId(source.getCustomerId()); row.setCustomerName(source.getCustomerName());
        row.setCompanyName(source.getCompanyName()); row.setCustomerEmail(source.getCustomerEmail());
        row.setCustomerPhone(source.getCustomerPhone()); row.setOwnerUserId(source.getOwnerUserId());
        row.setOwnerName(source.getOwnerName()); row.setCustomerPoNo(source.getCustomerPoNo());
        row.setRecipientName(source.getRecipientName()); row.setRecipientPhone(source.getRecipientPhone());
        row.setShippingAddress(source.getShippingAddress()); row.setCurrencyCode(source.getCurrencyCode());
        row.setListAmount(source.getListAmount()); row.setDiscountAmount(source.getDiscountAmount());
        row.setProductAmount(source.getProductAmount()); row.setShippingAmount(source.getShippingAmount());
        row.setTaxAmount(source.getTaxAmount()); row.setTotalAmount(source.getTotalAmount());
        row.setDiscountRate(weightedRate(source.getProductAmount(), source.getListAmount()));
        row.setDocumentStatus("SUBMITTED"); row.setPaymentStatus("UNPAID");
        row.setProductionStatus("PENDING"); row.setShipmentStatus("UNSHIPPED");
        row.setSubmittedTime(TimeUtils.utcNow()); row.setRemark(source.getRemark()); row.setDelFlag("0");
        return row;
    }

    SalesDocumentItem item(QuickOrder source, QuickOrderItem item) {
        SalesDocumentItem row = new SalesDocumentItem();
        row.setSourceQuickOrderItemId(item.getQuickOrderItemId()); row.setTenantId(source.getTenantId());
        row.setLineNo(item.getLineNo()); row.setItemCode(source.getQuickOrderNo() + "-" + item.getLineNo());
        row.setRoomLocation(item.getRoomLocation()); row.setSaleProductId(item.getSaleProductId());
        row.setSaleProductCode(item.getSaleProductCode()); row.setSaleProductName(item.getSaleProductName());
        row.setCategoryId(item.getCategoryId()); row.setCategoryCode(item.getCategoryCode());
        row.setCategoryNameCn(item.getCategoryNameCn()); row.setProductTypeCode(item.getProductTypeCode());
        row.setProductTypeNameCn(item.getProductTypeNameCn()); row.setFormulaId(item.getFormulaId());
        row.setFormulaVersionId(item.getFormulaVersionId()); row.setFormulaVersionLabel(item.getFormulaVersionLabel());
        row.setOrderWidthInch(item.getOrderWidthInch()); row.setOrderHeightInch(item.getOrderHeightInch());
        row.setQuantity(item.getQuantity()); row.setSelectedOptionsJson(item.getSelectedOptionsJson());
        row.setConfigurationSummary(StringUtils.blankToDefault(item.getConfigurationSummaryEn(),
            item.getConfigurationSummaryCn()));
        row.setConfigurationSummaryCn(item.getConfigurationSummaryCn());
        row.setConfigurationSummaryEn(item.getConfigurationSummaryEn());
        row.setCalculationStatus("PASS"); row.setListUnitAmount(item.getListUnitAmount());
        row.setListAmount(item.getListAmount()); row.setDiscountRate(item.getDiscountRate());
        row.setDiscountAmount(item.getDiscountAmount());
        row.setUnitAmount(item.getUnitAmount()); row.setProductAmount(item.getProductAmount());
        row.setShippingTemplateId(item.getShippingTemplateId()); row.setUnitShippingAmount(item.getUnitShippingAmount());
        row.setShippingAmount(item.getShippingAmount());
        row.setLineAmount(item.getLineAmount()); row.setBomSnapshotJson(item.getBomSnapshotJson());
        row.setPricingSnapshotJson(item.getPricingSnapshotJson()); row.setShippingSnapshotJson(item.getShippingSnapshotJson());
        row.setSortOrder(item.getSortOrder()); row.setRemark(item.getRemark()); row.setDelFlag("0");
        return row;
    }

    private BigDecimal weightedRate(BigDecimal product, BigDecimal list) {
        return list == null || list.signum() == 0 ? BigDecimal.ONE
            : product.divide(list, 4, RoundingMode.HALF_UP);
    }
}
