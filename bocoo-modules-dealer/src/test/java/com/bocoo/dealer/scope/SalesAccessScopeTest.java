package com.bocoo.dealer.scope;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SalesAccessScopeTest {

    @Test
    void resolvesPlatformRoleScopes() {
        assertEquals(SalesAccessScope.PLATFORM_ALL,
            SalesAccessScope.resolve(true, true, Set.of()));
        assertEquals(SalesAccessScope.PLATFORM_ALL,
            SalesAccessScope.resolve(true, false, Set.of("platform_sales_manager")));
        assertEquals(SalesAccessScope.PLATFORM_OWNED,
            SalesAccessScope.resolve(true, false, Set.of("platform_sales")));
        assertEquals(SalesAccessScope.PLATFORM_ALL,
            SalesAccessScope.resolve(true, false, Set.of("factory_production")));
    }

    @Test
    void resolvesMerchantRoleScopes() {
        assertEquals(SalesAccessScope.MERCHANT_ALL,
            SalesAccessScope.resolve(false, false, Set.of("merchant_admin")));
        assertEquals(SalesAccessScope.MERCHANT_OWNED,
            SalesAccessScope.resolve(false, false, Set.of("merchant_sales")));
    }
}
