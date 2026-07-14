package com.bocoo.dealer.scope;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.service.TestSaTokenContext;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SalesOwnershipResolverTest {
    private SalesStoreService salesStoreService;
    private SalesOwnershipResolver resolver;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        salesStoreService = mock(SalesStoreService.class);
        resolver = new SalesOwnershipResolver(salesStoreService);
    }

    @Test
    void merchantUsesCurrentTenantWithoutSalesStore() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, 20L,
            "seller", List.of());

        SalesOwnership value = resolver.current();

        assertThat(value).isEqualTo(new SalesOwnership(300001L, "MERCHANT", null, 20L, 7L));
        verifyNoInteractions(salesStoreService);
    }

    @Test
    void internalUsesPlatformTenantAndEnabledDepartmentStore() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, 12L,
            "internal", List.of());
        SalesStoreVo store = new SalesStoreVo();
        store.setSalesStoreId(31L);
        when(salesStoreService.resolveEnabledByDeptId(12L)).thenReturn(store);

        SalesOwnership value = resolver.current();

        assertThat(value).isEqualTo(new SalesOwnership(1L, "INTERNAL", 31L, 12L, 8L));
    }
}
