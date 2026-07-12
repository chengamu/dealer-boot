package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CustomerQuotePricingSessionFactory {
    private final CustomerQuoteCatalogService catalogService;

    CustomerQuotePricingSession create(Long tenantId) {
        return new CustomerQuotePricingSession(tenantId, catalogService);
    }
}
