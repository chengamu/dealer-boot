package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.service.ProductPriceRuntimeContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class CustomerQuotePricingSession {
    private final Long tenantId;
    private final CustomerQuoteCatalogService catalogService;
    private final Map<PricingKey, ProductPriceRuntimeContext> pricing = new HashMap<>();
    private final Map<Long, PricingKey> productKeys = new HashMap<>();
    private final Map<ShippingKey, Optional<ProductShippingTemplateVo>> shipping = new HashMap<>();

    CustomerQuotePricingSession(Long tenantId, CustomerQuoteCatalogService catalogService) {
        this.tenantId = tenantId;
        this.catalogService = catalogService;
    }

    ProductPriceRuntimeContext pricing(Long saleProductId) {
        PricingKey known = productKeys.get(saleProductId);
        if (known != null) return pricing.get(known);
        ProductPriceRuntimeContext loaded = catalogService.prepareRuntime(saleProductId);
        PricingKey key = new PricingKey(tenantId, loaded.saleProductId(), loaded.formulaVersionId(), loaded.currencyCode());
        productKeys.put(saleProductId, key);
        pricing.put(key, loaded);
        return loaded;
    }

    ProductShippingTemplateVo shipping(String currencyCode) {
        ShippingKey key = new ShippingKey(tenantId, currencyCode);
        return shipping.computeIfAbsent(key, ignored -> Optional.ofNullable(loadShipping(currencyCode))).orElse(null);
    }

    private ProductShippingTemplateVo loadShipping(String currencyCode) {
        ProductShippingTemplateBo query = new ProductShippingTemplateBo();
        query.setCurrencyCode(currencyCode);
        query.setStatus("ENABLED");
        List<ProductShippingTemplateVo> options = catalogService.queryShippingTemplates(query);
        if (options.isEmpty()) return null;
        return catalogService.queryShippingTemplate(options.get(0).getShippingTemplateId());
    }

    private record PricingKey(Long tenantId, Long saleProductId, Long formulaVersionId, String currencyCode) { }
    private record ShippingKey(Long tenantId, String currencyCode) { }
}
