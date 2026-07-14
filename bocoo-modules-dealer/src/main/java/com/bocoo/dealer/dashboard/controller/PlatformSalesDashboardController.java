package com.bocoo.dealer.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import com.bocoo.dealer.dashboard.service.SalesDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform-sales/dashboard")
public class PlatformSalesDashboardController {
    private final SalesDashboardService service;

    @SaCheckPermission("platform:sales:dashboard:view")
    @GetMapping
    public R<SalesDashboardVo> get() {
        return R.ok(service.getPlatformDashboard());
    }
}
