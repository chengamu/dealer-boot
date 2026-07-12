package com.bocoo.dealer.dashboard.scope;

import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.domain.vo.RoleVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SalesDashboardScopeResolverTest {
    private final SalesDashboardScopeResolver resolver = new SalesDashboardScopeResolver();

    @Test
    void platformSalesIsRestrictedToOwner() {
        LoginUser user = user("PLATFORM", 1L, 41L, "platform_sales");

        SalesDashboardScope scope = resolver.resolve(user);

        assertThat(scope.type()).isEqualTo("SELF");
        assertThat(scope.tenantId()).isNull();
        assertThat(scope.ownerUserId()).isEqualTo(41L);
    }

    @Test
    void platformManagerGetsAuthorizedCrossTenantScope() {
        LoginUser user = user("PLATFORM", 1L, 42L, "platform_sales_manager");

        SalesDashboardScope scope = resolver.resolve(user);

        assertThat(scope.type()).isEqualTo("PLATFORM_AUTHORIZED");
        assertThat(scope.isCrossTenant()).isTrue();
        assertThat(scope.ownerUserId()).isNull();
    }

    @Test
    void merchantSalesIsRestrictedByTenantAndOwner() {
        LoginUser user = user("MERCHANT", 300001L, 51L, "merchant_sales");

        SalesDashboardScope scope = resolver.resolve(user);

        assertThat(scope.type()).isEqualTo("SELF");
        assertThat(scope.tenantId()).isEqualTo(300001L);
        assertThat(scope.ownerUserId()).isEqualTo(51L);
    }

    @Test
    void capabilitiesFollowDownstreamPermissions() {
        LoginUser user = user("PLATFORM", 1L, 52L, "factory_production");
        user.setMenuPermission(Set.of("sales:dashboard:view", "dealer:fulfillment:production:list"));

        var capabilities = resolver.capabilities(user);

        assertThat(capabilities.fulfillment()).isTrue();
        assertThat(capabilities.production()).isTrue();
        assertThat(capabilities.shipment()).isFalse();
        assertThat(capabilities.quote()).isFalse();
        assertThat(capabilities.payment()).isFalse();
    }

    private LoginUser user(String tenantType, Long tenantId, Long userId, String roleKey) {
        LoginUser user = new LoginUser();
        user.setTenantType(tenantType);
        user.setTenantId(tenantId);
        user.setUserId(userId);
        user.setUsername("user-" + userId);
        user.setRolePermission(Set.of(roleKey));
        RoleVO role = new RoleVO();
        role.setRoleKey(roleKey);
        role.setRoleName(roleKey);
        user.setRoles(List.of(role));
        return user;
    }
}
