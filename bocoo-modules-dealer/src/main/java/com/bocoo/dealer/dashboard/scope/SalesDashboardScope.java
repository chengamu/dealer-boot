package com.bocoo.dealer.dashboard.scope;

public record SalesDashboardScope(
    String viewType,
    String label,
    Long tenantId,
    String businessOrigin,
    Long salesStoreId,
    String salesStoreName
) {
    public boolean isPlatformView() {
        return "PLATFORM".equals(viewType);
    }
}
