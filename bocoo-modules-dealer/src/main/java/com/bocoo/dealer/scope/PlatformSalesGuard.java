package com.bocoo.dealer.scope;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

@Component
public class PlatformSalesGuard {
    public void requirePlatform() {
        if (!LoginHelper.isPlatformTenant()) {
            throw ServiceException.ofMessageKey("dealer.sales.platformOnly");
        }
    }
}
