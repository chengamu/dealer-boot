package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
class CustomerQuoteItemFactory {

    private final CustomerQuoteJsonSupport jsonSupport;

    CustomerQuoteItem success(CustomerQuoteItemBo bo, ProductSaleProductVo product, ProductPriceQuoteVo price,
                              CustomerQuoteOptionSnapshot options, CustomerQuoteShippingResult shipping) {
        CustomerQuoteItem item = baseItem(bo);
        item.setSaleProductId(product.getSaleProductId());
        item.setSaleProductCode(product.getSaleProductCode());
        item.setSaleProductName(product.getSaleProductName());
        item.setCategoryId(product.getCategoryId());
        item.setCategoryCode(product.getCategoryCode());
        item.setCategoryNameCn(product.getCategoryNameCn());
        item.setProductTypeCode(product.getProductTypeCode());
        item.setProductTypeNameCn(product.getProductTypeNameCn());
        item.setFormulaId(product.getFormulaId());
        item.setFormulaVersionId(price.getFormulaVersionId());
        item.setFormulaVersionLabel(product.getFormulaVersionLabel());
        item.setSelectedOptionsJson(jsonSupport.write(options.selectedValues()));
        item.setSelectedOptionsSummaryCn(options.summaryCn());
        item.setSelectedOptionsSummaryEn(options.summaryEn());
        item.setCalculationStatus("PASS");
        item.setUnitAmount(money(price.getSingleAmount()));
        item.setProductAmount(money(price.getTotalAmount()));
        item.setUnitShippingAmount(shipping.unitAmount());
        item.setShippingTemplateId(shipping.templateId());
        item.setShippingAmount(money(shipping.unitAmount().multiply(BigDecimal.valueOf(bo.getQuantity()))));
        item.setDiscountAmount(money(null));
        item.setLineAmount(money(item.getProductAmount().add(item.getShippingAmount())));
        item.setBomSnapshotJson(jsonSupport.write(price.getItems()));
        item.setPricingSnapshotJson(jsonSupport.pricingSnapshot(price, shipping, options.englishComplete()));
        item.setShippingSnapshotJson(jsonSupport.shippingSnapshot(shipping, item.getShippingAmount(), price.getCurrencyCode()));
        return item;
    }

    CustomerQuoteItem failed(CustomerQuoteItemBo bo, String message, CustomerQuotePricingSession session) {
        CustomerQuoteItem item = baseItem(bo);
        item.setSaleProductId(bo.getSaleProductId());
        if (bo.getSaleProductId() != null) {
            fillProductSnapshot(item, bo.getSaleProductId(), session);
        }
        item.setSelectedOptionsJson(jsonSupport.write(bo.getSelectedOptionValues()));
        item.setCalculationStatus(bo.getSaleProductId() == null ? "PENDING" : "FAIL");
        item.setCalculationMessage(message);
        item.setUnitAmount(money(null));
        item.setProductAmount(money(null));
        item.setUnitShippingAmount(money(null));
        item.setShippingAmount(money(null));
        item.setDiscountAmount(money(null));
        item.setLineAmount(money(null));
        return item;
    }

    CustomerQuoteItemVo toVo(CustomerQuoteItem item) {
        CustomerQuoteItemVo vo = MapstructUtils.convert(item, CustomerQuoteItemVo.class);
        if (vo != null) {
            vo.setSelectedOptionValues(jsonSupport.readSelections(item.getSelectedOptionsJson()));
        }
        return vo;
    }

    private CustomerQuoteItem baseItem(CustomerQuoteItemBo bo) {
        CustomerQuoteItem item = new CustomerQuoteItem();
        item.setQuoteItemId(bo.getQuoteItemId());
        item.setRoomLocation(bo.getRoomLocation());
        item.setOrderWidthInch(bo.getOrderWidthInch());
        item.setOrderHeightInch(bo.getOrderHeightInch());
        item.setQuantity(bo.getQuantity() == null ? 1 : bo.getQuantity());
        item.setSortOrder(bo.getSortOrder());
        item.setRemark(bo.getRemark());
        item.setDelFlag("0");
        return item;
    }

    private void fillProductSnapshot(CustomerQuoteItem item, Long saleProductId, CustomerQuotePricingSession session) {
        try {
            ProductSaleProductVo product = session.pricing(saleProductId).setup().getSaleProduct();
            if (product == null) {
                return;
            }
            item.setSaleProductCode(product.getSaleProductCode());
            item.setSaleProductName(product.getSaleProductName());
            item.setCategoryId(product.getCategoryId());
            item.setCategoryCode(product.getCategoryCode());
            item.setCategoryNameCn(product.getCategoryNameCn());
            item.setProductTypeCode(product.getProductTypeCode());
            item.setProductTypeNameCn(product.getProductTypeNameCn());
            item.setFormulaId(product.getFormulaId());
            item.setFormulaVersionId(product.getFormulaVersionId());
            item.setFormulaVersionLabel(product.getFormulaVersionLabel());
        } catch (RuntimeException ignored) {
            // Preserve the draft line and its original calculation error.
        }
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
