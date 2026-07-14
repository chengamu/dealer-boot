package com.bocoo.dealer.scope;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("dealerSalesOwnershipResolver")
@RequiredArgsConstructor
public class SalesOwnershipResolver {
    private static final Long PLATFORM_TENANT_ID = 1L;

    private final SalesStoreService salesStoreService;

    public SalesOwnership current() {
        Long tenantId = require(LoginHelper.getTenantId(), "tenant.context.missing");
        Long ownerUserId = LoginHelper.getUserId();
        if (ownerUserId == null) {
            throw ServiceException.ofMessageKey("user.not.exists", LoginHelper.getUsername());
        }
        Long deptId = LoginHelper.getDeptId();
        if (!LoginHelper.isPlatformTenant()) {
            return new SalesOwnership(tenantId, "MERCHANT", null, deptId, ownerUserId);
        }
        SalesStoreVo store = salesStoreService.resolveEnabledByDeptId(deptId);
        return new SalesOwnership(PLATFORM_TENANT_ID, "INTERNAL", store.getSalesStoreId(), deptId, ownerUserId);
    }

    private Long require(Long value, String messageKey) {
        if (value == null) throw ServiceException.ofMessageKey(messageKey);
        return value;
    }
}
