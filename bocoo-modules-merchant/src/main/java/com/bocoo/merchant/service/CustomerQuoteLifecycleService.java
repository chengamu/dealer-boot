package com.bocoo.merchant.service;

public interface CustomerQuoteLifecycleService {
    Boolean confirm(Long id);

    Boolean voidQuote(Long id);
}
