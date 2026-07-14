package com.bocoo.dealer.scope;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformSalesGuardTest {
    private final PlatformSalesGuard guard = new PlatformSalesGuard();

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
    }

    @Test
    void merchantCannotEnterPlatformQueryService() {
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        assertThatThrownBy(guard::requirePlatform).isInstanceOf(ServiceException.class);
    }

    @Test
    void platformIdentityCanEnterPlatformQueryService() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "operator");
        assertThatCode(guard::requirePlatform).doesNotThrowAnyException();
    }
}
