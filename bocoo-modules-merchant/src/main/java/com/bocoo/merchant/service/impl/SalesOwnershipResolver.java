package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("merchantSalesOwnershipResolver")
@RequiredArgsConstructor
public class SalesOwnershipResolver extends MerchantServiceSupport {

    private final SalesStoreService salesStoreService;

    SalesOwnership currentForCreate() {
        Long deptId = LoginHelper.getDeptId();
        if (deptId == null) {
            throw ServiceException.ofMessageKey("sales.store.dept.required");
        }
        Long tenantId = currentTenantId();
        Long ownerUserId = LoginHelper.getUserId();
        if (!LoginHelper.isPlatformTenant()) {
            return new SalesOwnership(tenantId, SalesOwnership.MERCHANT, null, deptId, ownerUserId);
        }
        SalesStoreVo store = salesStoreService.resolveEnabledByDeptId(deptId);
        return new SalesOwnership(PLATFORM_TENANT_ID, SalesOwnership.INTERNAL,
            store.getSalesStoreId(), deptId, ownerUserId);
    }

    String currentBusinessOrigin() {
        return LoginHelper.isPlatformTenant() ? SalesOwnership.INTERNAL : SalesOwnership.MERCHANT;
    }
}
