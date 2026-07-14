package com.bocoo.dealer.scope;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;

public record SalesBusinessScope(Long tenantId, String businessOrigin) {
    private static final Long PLATFORM_TENANT_ID = 1L;

    public static SalesBusinessScope current() {
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null) throw ServiceException.ofMessageKey("tenant.context.missing");
        return LoginHelper.isPlatformTenant()
            ? new SalesBusinessScope(PLATFORM_TENANT_ID, "INTERNAL")
            : new SalesBusinessScope(tenantId, "MERCHANT");
    }
}
