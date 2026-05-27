package com.bocoo.common.core.context;

import java.util.function.Supplier;

public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IGNORE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TenantContextHolder() {
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant id is required");
        }
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    public static void setIgnore(Boolean ignore) {
        IGNORE.set(Boolean.TRUE.equals(ignore));
    }

    public static void runWithIgnore(Runnable runnable) {
        callWithIgnore(() -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T callWithIgnore(Supplier<T> supplier) {
        Long previousTenantId = TENANT_ID.get();
        Boolean previousIgnore = IGNORE.get();
        try {
            TENANT_ID.remove();
            setIgnore(true);
            return supplier.get();
        } finally {
            if (previousTenantId == null) {
                TENANT_ID.remove();
            } else {
                TENANT_ID.set(previousTenantId);
            }
            IGNORE.set(Boolean.TRUE.equals(previousIgnore));
        }
    }

    public static void runWithTenant(Long tenantId, Runnable runnable) {
        callWithTenant(tenantId, () -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T callWithTenant(Long tenantId, Supplier<T> supplier) {
        Long previousTenantId = TENANT_ID.get();
        Boolean previousIgnore = IGNORE.get();
        try {
            setTenantId(tenantId);
            setIgnore(false);
            return supplier.get();
        } finally {
            if (previousTenantId == null) {
                TENANT_ID.remove();
            } else {
                TENANT_ID.set(previousTenantId);
            }
            IGNORE.set(Boolean.TRUE.equals(previousIgnore));
        }
    }

    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }
}
