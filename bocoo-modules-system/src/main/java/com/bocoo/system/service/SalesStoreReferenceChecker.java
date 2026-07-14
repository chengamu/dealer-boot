package com.bocoo.system.service;

import com.bocoo.system.domain.vo.SalesStoreReferenceVo;

/**
 * Extension point for customer/order modules to report store references.
 */
public interface SalesStoreReferenceChecker {

    SalesStoreReferenceVo check(Long salesStoreId);
}
