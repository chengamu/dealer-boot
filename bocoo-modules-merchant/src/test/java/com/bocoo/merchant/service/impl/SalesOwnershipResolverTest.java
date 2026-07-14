package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.merchant.service.TestSaTokenContext;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesOwnershipResolverTest {

    @Mock
    private SalesStoreService salesStoreService;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
    }

    @Test
    void merchantUsesCurrentTenantWithoutSalesStore() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300L, 9L, "merchant-sales", 31L);

        SalesOwnership ownership = new SalesOwnershipResolver(salesStoreService).currentForCreate();

        assertThat(ownership.tenantId()).isEqualTo(300L);
        assertThat(ownership.businessOrigin()).isEqualTo("MERCHANT");
        assertThat(ownership.salesStoreId()).isNull();
        assertThat(ownership.deptId()).isEqualTo(31L);
        assertThat(ownership.ownerUserId()).isEqualTo(9L);
        verifyNoInteractions(salesStoreService);
    }

    @Test
    void internalUsesPlatformTenantAndEnabledDepartmentStore() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "internal-sales", 21L);
        SalesStoreVo store = new SalesStoreVo();
        store.setSalesStoreId(501L);
        when(salesStoreService.resolveEnabledByDeptId(21L)).thenReturn(store);

        SalesOwnership ownership = new SalesOwnershipResolver(salesStoreService).currentForCreate();

        assertThat(ownership.tenantId()).isEqualTo(1L);
        assertThat(ownership.businessOrigin()).isEqualTo("INTERNAL");
        assertThat(ownership.salesStoreId()).isEqualTo(501L);
        assertThat(ownership.deptId()).isEqualTo(21L);
        assertThat(ownership.ownerUserId()).isEqualTo(8L);
        verify(salesStoreService).resolveEnabledByDeptId(21L);
    }
}
