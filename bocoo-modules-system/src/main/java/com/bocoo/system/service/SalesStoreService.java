package com.bocoo.system.service;

import com.bocoo.system.domain.vo.SalesStoreVo;

/**
 * Cross-module sales store lookup contract.
 */
public interface SalesStoreService {

    SalesStoreVo resolveEnabledByDeptId(Long deptId);

    SalesStoreVo queryById(Long salesStoreId);

    boolean isDeptLinked(Long deptId);
}
