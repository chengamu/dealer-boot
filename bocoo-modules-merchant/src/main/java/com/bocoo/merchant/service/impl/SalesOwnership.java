package com.bocoo.merchant.service.impl;

record SalesOwnership(Long tenantId, String businessOrigin, Long salesStoreId,
                      Long deptId, Long ownerUserId) {

    static final String INTERNAL = "INTERNAL";
    static final String MERCHANT = "MERCHANT";
}
