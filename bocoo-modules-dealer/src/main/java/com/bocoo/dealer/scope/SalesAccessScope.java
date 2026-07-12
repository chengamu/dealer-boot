package com.bocoo.dealer.scope;

import java.util.Collections;
import java.util.Set;

/**
 * Sales document visibility derived from tenant type and role capabilities.
 */
public enum SalesAccessScope {
    PLATFORM_ALL(true, false),
    PLATFORM_OWNED(true, true),
    MERCHANT_ALL(false, false),
    MERCHANT_OWNED(false, true);

    private static final Set<String> PLATFORM_ALL_ROLES = Set.of(
        "admin", "platform_sales_manager", "platform_finance", "factory_production", "factory_shipping"
    );
    private static final Set<String> MERCHANT_ALL_ROLES = Set.of(
        "merchant_admin", "merchant_finance", "merchant_fulfillment"
    );

    private final boolean crossTenant;
    private final boolean ownerOnly;

    SalesAccessScope(boolean crossTenant, boolean ownerOnly) {
        this.crossTenant = crossTenant;
        this.ownerOnly = ownerOnly;
    }

    public static SalesAccessScope resolve(boolean platform, boolean administrator, Set<String> roles) {
        Set<String> safeRoles = roles == null ? Collections.emptySet() : roles;
        if (platform) {
            return administrator || containsAny(safeRoles, PLATFORM_ALL_ROLES)
                ? PLATFORM_ALL : PLATFORM_OWNED;
        }
        return administrator || containsAny(safeRoles, MERCHANT_ALL_ROLES)
            ? MERCHANT_ALL : MERCHANT_OWNED;
    }

    public boolean crossTenant() {
        return crossTenant;
    }

    public boolean ownerOnly() {
        return ownerOnly;
    }

    private static boolean containsAny(Set<String> actual, Set<String> expected) {
        return actual.stream().anyMatch(expected::contains);
    }
}
