package com.bocoo.dealer.dashboard.scope;

public record SalesDashboardScope(
    String type,
    String label,
    Long tenantId,
    Long ownerUserId
) {
    public boolean isCrossTenant() {
        return tenantId == null;
    }
}
