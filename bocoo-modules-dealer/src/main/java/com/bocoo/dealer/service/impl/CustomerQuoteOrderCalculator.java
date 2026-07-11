package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderLinePreviewVo;
import com.bocoo.dealer.domain.vo.CustomerQuoteOrderPreviewVo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.service.CustomerQuoteConversionSnapshot;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
class CustomerQuoteOrderCalculator {
    private final SalesDiscountResolver discounts;

    CustomerQuoteOrderCalculation calculate(CustomerQuoteConversionSnapshot snapshot) {
        CustomerQuote quote = snapshot.quote();
        if (!"CONFIRMED".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.order.confirmedOnly");
        }
        if (snapshot.items().isEmpty() || snapshot.items().stream().anyMatch(this::invalidItem)) {
            throw ServiceException.ofMessageKey("customer.quote.order.snapshotInvalid");
        }
        MerchantProfileVo profile = discounts.profile(quote.getTenantId());
        CustomerQuoteOrderPreviewVo preview = basePreview(quote, profile);
        LinkedHashMap<Long, BigDecimal> rates = new LinkedHashMap<>();
        BigDecimal list = BigDecimal.ZERO, product = BigDecimal.ZERO, shipping = BigDecimal.ZERO;
        for (CustomerQuoteItem item : snapshot.items()) {
            BigDecimal rate = discounts.resolve(profile, item.getCategoryId(), item.getProductTypeCode());
            BigDecimal listAmount = money(item.getProductAmount());
            BigDecimal productAmount = money(listAmount.multiply(rate));
            BigDecimal shippingAmount = money(item.getShippingAmount());
            preview.getItems().add(line(item, rate, listAmount, productAmount, shippingAmount));
            rates.put(item.getQuoteItemId(), rate);
            list = list.add(listAmount); product = product.add(productAmount); shipping = shipping.add(shippingAmount);
        }
        preview.setListAmount(money(list));
        preview.setProductAmount(money(product));
        preview.setDiscountAmount(money(list.subtract(product)));
        preview.setShippingAmount(money(shipping));
        preview.setTotalAmount(money(product.add(shipping)));
        return new CustomerQuoteOrderCalculation(profile, preview, rates);
    }

    private CustomerQuoteOrderPreviewVo basePreview(CustomerQuote quote, MerchantProfileVo profile) {
        CustomerQuoteOrderPreviewVo vo = new CustomerQuoteOrderPreviewVo();
        vo.setQuoteId(quote.getQuoteId()); vo.setQuoteNo(quote.getQuoteNo());
        vo.setCustomerName(quote.getCustomerName()); vo.setProjectName(quote.getProjectName());
        vo.setMerchantLevelCode(profile == null ? null : profile.getLevelCode());
        vo.setMerchantLevelName(profile == null ? null : profile.getLevelName());
        vo.setCurrencyCode(quote.getCurrencyCode()); vo.setCustomerQuoteAmount(money(quote.getTotalAmount()));
        return vo;
    }

    private CustomerQuoteOrderLinePreviewVo line(CustomerQuoteItem item, BigDecimal rate, BigDecimal list,
                                                  BigDecimal product, BigDecimal shipping) {
        CustomerQuoteOrderLinePreviewVo vo = new CustomerQuoteOrderLinePreviewVo();
        vo.setQuoteItemId(item.getQuoteItemId()); vo.setLineNo(item.getLineNo());
        vo.setRoomLocation(item.getRoomLocation()); vo.setSaleProductName(item.getSaleProductName());
        vo.setQuantity(item.getQuantity()); vo.setListAmount(list); vo.setDiscountRate(rate);
        vo.setDiscountAmount(money(list.subtract(product))); vo.setProductAmount(product);
        vo.setShippingAmount(shipping); vo.setLineAmount(money(product.add(shipping)));
        return vo;
    }

    private boolean invalidItem(CustomerQuoteItem item) {
        return !"PASS".equals(item.getCalculationStatus()) || item.getFormulaVersionId() == null
            || item.getQuoteItemId() == null || item.getQuantity() == null || item.getQuantity() <= 0
            || item.getProductAmount() == null || item.getShippingAmount() == null
            || item.getShippingTemplateId() == null || item.getPricingSnapshotJson() == null
            || item.getBomSnapshotJson() == null;
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
