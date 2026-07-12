package com.bocoo.pay.service;

import com.bocoo.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

@Component
public class PayOperatorContext {
    public Long tenantId() {
        return LoginHelper.getTenantId();
    }

    public boolean isPlatform() {
        return LoginHelper.isPlatformTenant();
    }

    public Long userId() {
        return LoginHelper.getUserId();
    }

    public String username() {
        return LoginHelper.getUsername();
    }
}
