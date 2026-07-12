package com.bocoo.dealer.dashboard.scope;

import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.domain.vo.RoleVO;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SalesDashboardScopeResolver {
    private static final String ALL_PERMISSION = "*:*:*";

    public SalesDashboardScope resolve() {
        return resolve(LoginHelper.getLoginUser());
    }

    public SalesDashboardScope resolve(LoginUser user) {
        if (user == null) throw new IllegalStateException("Login user is required");
        Set<String> roles = roleKeys(user);
        if (!"PLATFORM".equals(user.getTenantType())) {
            boolean tenantWide = LoginHelper.isAdmin(user.getUserId())
                || roles.stream().anyMatch(Set.of("merchant_admin", "merchant_finance", "merchant_fulfillment")::contains);
            return tenantWide
                ? new SalesDashboardScope("TENANT", scopeLabel(user, roles), user.getTenantId(), null)
                : new SalesDashboardScope("SELF", user.getUsername(), user.getTenantId(), user.getUserId());
        }
        if (LoginHelper.isAdmin(user.getUserId())) {
            return new SalesDashboardScope("PLATFORM_ALL", scopeLabel(user, roles), null, null);
        }
        if (roles.contains("factory_production") || roles.contains("factory_shipping")) {
            return new SalesDashboardScope("FULFILLMENT", scopeLabel(user, roles), null, null);
        }
        if (roles.contains("platform_finance")) {
            return new SalesDashboardScope("PLATFORM_FINANCE", scopeLabel(user, roles), null, null);
        }
        if (roles.contains("platform_sales_manager")) {
            return new SalesDashboardScope("PLATFORM_AUTHORIZED", scopeLabel(user, roles), null, null);
        }
        return new SalesDashboardScope("SELF", user.getUsername(), null, user.getUserId());
    }

    public SalesDashboardVo.Capabilities capabilities() {
        return capabilities(LoginHelper.getLoginUser());
    }

    public SalesDashboardVo.Capabilities capabilities(LoginUser user) {
        Set<String> permissions = user == null || user.getMenuPermission() == null
            ? Set.of() : user.getMenuPermission();
        boolean admin = user != null && (LoginHelper.isAdmin(user.getUserId()) || permissions.contains(ALL_PERMISSION));
        boolean production = admin || hasAny(permissions, "dealer:fulfillment:production:list",
            "dealer:fulfillment:production:query");
        boolean shipment = admin || hasAny(permissions, "dealer:fulfillment:shipment:list",
            "dealer:fulfillment:shipment:query", "dealer:fulfillment:tracking:list");
        return new SalesDashboardVo.Capabilities(
            admin || permissions.contains("customer:quote:list"),
            admin || permissions.contains("customer:quote:query"),
            admin || permissions.contains("dealer:sales:list"),
            admin || permissions.contains("dealer:sales:query"),
            admin || hasAny(permissions, "pay:order:list", "pay:order:query"),
            production || shipment,
            production,
            shipment,
            admin || permissions.contains("customer:quote:add"),
            admin || permissions.contains("customer:profile:add"),
            admin || permissions.contains("dealer:quick-order:add")
        );
    }

    private Set<String> roleKeys(LoginUser user) {
        Set<String> keys = new HashSet<>();
        if (user.getRolePermission() != null) keys.addAll(user.getRolePermission());
        List<RoleVO> roles = user.getRoles();
        if (roles != null) roles.stream().map(RoleVO::getRoleKey).filter(key -> key != null && !key.isBlank()).forEach(keys::add);
        return keys;
    }

    private String scopeLabel(LoginUser user, Set<String> roles) {
        if (user.getRoles() != null) {
            String name = user.getRoles().stream().filter(role -> roles.contains(role.getRoleKey()))
                .map(RoleVO::getRoleName).filter(value -> value != null && !value.isBlank()).findFirst().orElse(null);
            if (name != null) return name;
        }
        return user.getUsername();
    }

    private boolean hasAny(Set<String> permissions, String... values) {
        for (String value : values) if (permissions.contains(value)) return true;
        return false;
    }
}
