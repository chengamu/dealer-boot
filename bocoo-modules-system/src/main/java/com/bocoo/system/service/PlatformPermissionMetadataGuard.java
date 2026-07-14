package com.bocoo.system.service;

import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

@Component
public class PlatformPermissionMetadataGuard {

    public void requirePlatformTenant() {
        if (!LoginHelper.isPlatformTenant()
            || !TenantConstants.PLATFORM_TENANT_ID.equals(LoginHelper.getTenantId())) {
            throw ServiceException.ofMessageKey("sys.permission.metadata.platform.only");
        }
    }
}
