package com.bocoo.merchant.service.impl;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CustomerQuoteReadScopeTest {
    @Test
    void limitsOrdinarySalesAndKeepsManagerScope() {
        assertArrayEquals(new Object[]{null, 9L, true},
            values(CustomerQuoteReadScope.resolve(true, false, 1L, 9L, Set.of("platform_sales"))));
        assertArrayEquals(new Object[]{null, null, true},
            values(CustomerQuoteReadScope.resolve(true, false, 1L, 9L, Set.of("platform_sales_manager"))));
        assertArrayEquals(new Object[]{300001L, 9L, false},
            values(CustomerQuoteReadScope.resolve(false, false, 300001L, 9L, Set.of("merchant_sales"))));
    }

    private Object[] values(CustomerQuoteReadScope scope) {
        return new Object[]{scope.tenantId(), scope.ownerUserId(), scope.crossTenant()};
    }
}
