package com.bocoo.dealer.dashboard.scope;

import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesDashboardScopeResolver {
    private static final Long PLATFORM_TENANT_ID = 1L;
    private final SalesStoreService salesStoreService;

    public SalesDashboardScope resolveBusiness() {
        return resolveBusiness(requireUser());
    }

    SalesDashboardScope resolveBusiness(LoginUser user) {
        if ("MERCHANT".equals(user.getTenantType())) {
            return new SalesDashboardScope("MERCHANT", user.getUsername(), requireTenant(user),
                "MERCHANT", null, null);
        }
        if ("PLATFORM".equals(user.getTenantType())) {
            SalesStoreVo store = salesStoreService.resolveEnabledByDeptId(user.getDeptId());
            return new SalesDashboardScope("INTERNAL", user.getUsername(), PLATFORM_TENANT_ID,
                "INTERNAL", store.getSalesStoreId(), store.getStoreName());
        }
        throw ServiceException.ofMessageKey("auth.permission.denied");
    }

    public SalesDashboardScope resolvePlatform() {
        return resolvePlatform(requireUser());
    }

    SalesDashboardScope resolvePlatform(LoginUser user) {
        if (!"PLATFORM".equals(user.getTenantType())) {
            throw ServiceException.ofMessageKey("auth.permission.denied");
        }
        return new SalesDashboardScope("PLATFORM", user.getUsername(), null, null, null, null);
    }

    private LoginUser requireUser() {
        LoginUser user = LoginHelper.getLoginUser();
        if (user == null) throw ServiceException.ofMessageKey("auth.unauthorized");
        return user;
    }

    private Long requireTenant(LoginUser user) {
        if (user.getTenantId() == null) throw ServiceException.ofMessageKey("tenant.context.missing");
        return user.getTenantId();
    }
}
