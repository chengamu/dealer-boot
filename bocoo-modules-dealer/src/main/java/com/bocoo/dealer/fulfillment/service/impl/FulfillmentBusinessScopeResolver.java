package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class FulfillmentBusinessScopeResolver {
    private static final Set<String> MERCHANT_TENANT_ROLES = Set.of("merchant_admin", "merchant_fulfillment");

    public FulfillmentBusinessScope resolve() {
        LoginUser user = LoginHelper.getLoginUser();
        if (user == null || user.getTenantId() == null || user.getUserId() == null) {
            throw ServiceException.ofMessageKey("tenant.context.missing");
        }
        Set<String> roles = user.getRolePermission() == null ? Collections.emptySet() : user.getRolePermission();
        if (!LoginHelper.isPlatformTenant()) {
            Long owner = LoginHelper.isAdmin() || containsAny(roles, MERCHANT_TENANT_ROLES)
                ? null : user.getUserId();
            return new FulfillmentBusinessScope(user.getTenantId(), "MERCHANT", null, owner);
        }
        if (LoginHelper.isAdmin()) {
            return new FulfillmentBusinessScope(user.getTenantId(), "INTERNAL", null, null);
        }
        if (roles.contains("platform_sales_manager") && user.getDeptId() != null) {
            return new FulfillmentBusinessScope(user.getTenantId(), "INTERNAL", user.getDeptId(), null);
        }
        return new FulfillmentBusinessScope(user.getTenantId(), "INTERNAL", null, user.getUserId());
    }

    private boolean containsAny(Set<String> actual, Set<String> expected) {
        return actual.stream().anyMatch(expected::contains);
    }
}
