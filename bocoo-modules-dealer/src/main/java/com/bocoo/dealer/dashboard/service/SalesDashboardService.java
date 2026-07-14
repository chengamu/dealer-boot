package com.bocoo.dealer.dashboard.service;

import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;

public interface SalesDashboardService {
    SalesDashboardVo getBusinessDashboard();

    SalesDashboardVo getPlatformDashboard();
}
