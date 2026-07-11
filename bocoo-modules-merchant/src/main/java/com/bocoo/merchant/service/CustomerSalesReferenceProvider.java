package com.bocoo.merchant.service;

public interface CustomerSalesReferenceProvider {
    long countByCustomer(Long tenantId, Long customerId);
}
