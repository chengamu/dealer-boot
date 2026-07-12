package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.product.service.ProductPriceRuntimeContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
class CustomerQuoteCalculator {

    private final CustomerQuoteOptionSnapshotBuilder optionSnapshotBuilder;
    private final CustomerQuoteShippingCalculator shippingCalculator;
    private final CustomerQuoteItemFactory itemFactory;

    CustomerQuoteCalculatedItem calculate(CustomerQuoteItemBo bo, CustomerQuotePricingSession session) {
        validateInputs(bo);
        ProductPriceRuntimeContext runtime = session.pricing(bo.getSaleProductId());
        ProductPriceSetupVo setup = runtime.setup();
        ProductSaleProductVo product = setup.getSaleProduct();
        if (product == null || !"ENABLED".equalsIgnoreCase(product.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.product.unavailable");
        }
        ProductPriceQuoteBo request = priceRequest(bo);
        ProductPriceQuoteVo price = runtime.quote(request);
        CustomerQuoteOptionSnapshot options = optionSnapshotBuilder.build(setup, bo.getSelectedOptionValues());
        CustomerQuoteShippingResult shipping = shippingCalculator.calculate(session, price.getCurrencyCode(),
            bo.getOrderWidthInch(), bo.getOrderHeightInch(), options.motorized());
        return new CustomerQuoteCalculatedItem(itemFactory.success(bo, product, price, options, shipping), price.getCurrencyCode());
    }

    CustomerQuoteItemVo toVo(CustomerQuoteItem item) {
        return itemFactory.toVo(item);
    }

    CustomerQuoteItem failed(CustomerQuoteItemBo bo, String message, CustomerQuotePricingSession session) {
        return itemFactory.failed(bo, message, session);
    }

    CustomerQuoteOptionSnapshot optionSnapshot(Long saleProductId, Map<String, String> selections,
                                                CustomerQuotePricingSession session) {
        return optionSnapshotBuilder.build(session.pricing(saleProductId).setup(), selections);
    }

    private ProductPriceQuoteBo priceRequest(CustomerQuoteItemBo bo) {
        ProductPriceQuoteBo request = new ProductPriceQuoteBo();
        request.setOrderWidth(bo.getOrderWidthInch());
        request.setOrderHeight(bo.getOrderHeightInch());
        request.setOrderQuantity(bo.getQuantity());
        request.setSelectedOptionValues(bo.getSelectedOptionValues());
        return request;
    }

    private void validateInputs(CustomerQuoteItemBo bo) {
        if (bo.getSaleProductId() == null || bo.getOrderWidthInch() == null || bo.getOrderHeightInch() == null
            || bo.getOrderWidthInch().signum() <= 0 || bo.getOrderHeightInch().signum() <= 0
            || bo.getQuantity() == null || bo.getQuantity() < 1) {
            throw ServiceException.ofMessageKey("customer.quote.item.incomplete");
        }
    }

}
