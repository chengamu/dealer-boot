package com.bocoo.dealer.payment;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.dealer.scope.SalesOwnership;
import com.bocoo.dealer.scope.SalesOwnershipResolver;
import com.bocoo.dealer.service.TestSaTokenContext;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.credit.CreditSubjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalesCreditSubjectScopeResolverTest {
    private SalesOwnershipResolver ownershipResolver;
    private SalesCreditSubjectScopeResolver resolver;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        ownershipResolver = mock(SalesOwnershipResolver.class);
        resolver = new SalesCreditSubjectScopeResolver(ownershipResolver);
    }

    @Test
    void merchantCreditIdentityIsCurrentTenant() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "merchant-user");
        when(ownershipResolver.current()).thenReturn(new SalesOwnership(300001L, "MERCHANT", null, 20L, 7L));

        CreditSubject subject = resolver.current("USD");

        assertThat(subject.type()).isEqualTo(CreditSubjectType.MERCHANT);
        assertThat(subject.tenantId()).isEqualTo(300001L);
        assertThat(subject.salesStoreId()).isNull();
    }

    @Test
    void internalCreditIdentityIsCurrentSalesStore() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "internal-user");
        when(ownershipResolver.current()).thenReturn(new SalesOwnership(1L, "INTERNAL", 31L, 12L, 8L));

        CreditSubject subject = resolver.current("USD");

        assertThat(subject.type()).isEqualTo(CreditSubjectType.INTERNAL);
        assertThat(subject.tenantId()).isEqualTo(1L);
        assertThat(subject.salesStoreId()).isEqualTo(31L);
    }
}
