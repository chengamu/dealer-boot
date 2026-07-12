package com.bocoo.merchant.service.impl;

import com.bocoo.common.satoken.utils.LoginHelper;

import java.util.Collections;
import java.util.Set;

record CustomerQuoteReadScope(Long tenantId, Long ownerUserId, boolean crossTenant) {
    private static final Set<String> PLATFORM_ALL = Set.of("admin", "platform_sales_manager");
    private static final Set<String> MERCHANT_ALL = Set.of(
        "merchant_admin", "merchant_finance", "merchant_fulfillment"
    );

    static CustomerQuoteReadScope current() {
        Set<String> roles = LoginHelper.getLoginUser() == null
            || LoginHelper.getLoginUser().getRolePermission() == null
            ? Collections.emptySet() : LoginHelper.getLoginUser().getRolePermission();
        return resolve(LoginHelper.isPlatformTenant(), LoginHelper.isAdmin(), LoginHelper.getTenantId(),
            LoginHelper.getUserId(), roles);
    }

    static CustomerQuoteReadScope resolve(boolean platform, boolean admin, Long tenantId,
                                          Long userId, Set<String> roles) {
        Set<String> safeRoles = roles == null ? Collections.emptySet() : roles;
        if (platform) {
            boolean all = admin || safeRoles.stream().anyMatch(PLATFORM_ALL::contains);
            return new CustomerQuoteReadScope(null, all ? null : userId, true);
        }
        boolean all = admin || safeRoles.stream().anyMatch(MERCHANT_ALL::contains);
        return new CustomerQuoteReadScope(tenantId, all ? null : userId, false);
    }
}
