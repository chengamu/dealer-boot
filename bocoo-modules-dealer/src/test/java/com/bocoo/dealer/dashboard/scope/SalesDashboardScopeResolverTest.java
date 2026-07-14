package com.bocoo.dealer.dashboard.scope;

import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalesDashboardScopeResolverTest {
    private SalesStoreService salesStoreService;
    private SalesDashboardScopeResolver resolver;

    @BeforeEach
    void setUp() {
        salesStoreService = mock(SalesStoreService.class);
        resolver = new SalesDashboardScopeResolver(salesStoreService);
    }

    @Test
    void merchantBusinessScopeUsesTenantAndMerchantOriginRegardlessOfRoleName() {
        LoginUser user = user("MERCHANT", 300001L, 51L);

        SalesDashboardScope scope = resolver.resolveBusiness(user);

        assertThat(scope.viewType()).isEqualTo("MERCHANT");
        assertThat(scope.tenantId()).isEqualTo(300001L);
        assertThat(scope.businessOrigin()).isEqualTo("MERCHANT");
        assertThat(scope.salesStoreId()).isNull();
    }

    @Test
    void internalBusinessScopeUsesStoreOnlyAsSourceContext() {
        LoginUser user = user("PLATFORM", 1L, 52L);
        user.setDeptId(12L);
        SalesStoreVo store = new SalesStoreVo();
        store.setSalesStoreId(31L);
        store.setStoreName("Internal Sales");
        when(salesStoreService.resolveEnabledByDeptId(12L)).thenReturn(store);

        SalesDashboardScope scope = resolver.resolveBusiness(user);

        assertThat(scope.viewType()).isEqualTo("INTERNAL");
        assertThat(scope.tenantId()).isEqualTo(1L);
        assertThat(scope.businessOrigin()).isEqualTo("INTERNAL");
        assertThat(scope.salesStoreId()).isEqualTo(31L);
        assertThat(scope.salesStoreName()).isEqualTo("Internal Sales");
    }

    @Test
    void platformEndpointCreatesExplicitCrossTenantScopeOnlyForPlatformIdentity() {
        SalesDashboardScope scope = resolver.resolvePlatform(user("PLATFORM", 1L, 53L));

        assertThat(scope.isPlatformView()).isTrue();
        assertThat(scope.tenantId()).isNull();
        assertThat(scope.businessOrigin()).isNull();

        assertThatThrownBy(() -> resolver.resolvePlatform(user("MERCHANT", 300001L, 54L)))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void platformViewNeverReturnsBusinessCreationCapabilities() {
        LoginUser user = user("PLATFORM", 1L, 55L);
        user.setMenuPermission(Set.of("customer:quote:add", "customer:profile:add", "dealer:quick-order:add"));

        var capabilities = new SalesDashboardCapabilitiesResolver().resolve(user, true);

        assertThat(capabilities.createQuote()).isFalse();
        assertThat(capabilities.createCustomer()).isFalse();
        assertThat(capabilities.quickOrder()).isFalse();
    }

    @Test
    void platformViewUsesDedicatedReadOnlySalesPermissions() {
        LoginUser user = user("PLATFORM", 1L, 56L);
        user.setMenuPermission(Set.of("platform:sales:quote:list", "platform:sales:order:list"));

        var capabilities = new SalesDashboardCapabilitiesResolver().resolve(user, true);

        assertThat(capabilities.quote()).isTrue();
        assertThat(capabilities.order()).isTrue();
        assertThat(capabilities.payment()).isFalse();
        assertThat(capabilities.production()).isFalse();
        assertThat(capabilities.shipment()).isFalse();
        assertThat(capabilities.createQuote()).isFalse();
        assertThat(capabilities.createCustomer()).isFalse();
        assertThat(capabilities.quickOrder()).isFalse();
    }

    private LoginUser user(String tenantType, Long tenantId, Long userId) {
        LoginUser user = new LoginUser();
        user.setTenantType(tenantType);
        user.setTenantId(tenantId);
        user.setUserId(userId);
        user.setUsername("user-" + userId);
        return user;
    }
}
