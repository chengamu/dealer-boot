package com.bocoo.merchant.service;

public interface CustomerQuoteLifecycleService {
    Boolean confirm(Long tenantId, Long id);

    Boolean voidQuote(Long id);
}
