package com.bocoo.system.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.bo.SysMenuBo;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysRoleDeptMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PlatformPermissionMetadataGuardTest {

    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysRoleDeptMapper roleDeptMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    private PlatformPermissionMetadataGuard guard;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 2001L, 10L, "merchant");
        guard = new PlatformPermissionMetadataGuard();
    }

    @Test
    void merchantTenantCannotWritePlatformMenu() {
        SysMenuCommandService service = new SysMenuCommandService(menuMapper, guard);

        assertPlatformOnly(() -> service.insertMenu(new SysMenuBo()));
        verifyNoInteractions(menuMapper);
    }

    @Test
    void merchantTenantCannotWritePlatformRoleRelations() {
        SysRoleRelationService service = new SysRoleRelationService(
            roleMenuMapper, roleDeptMapper, userRoleMapper, guard);

        assertPlatformOnly(() -> service.insertMenus(20L, new Long[]{30L}));
        verifyNoInteractions(roleMenuMapper);
    }

    private void assertPlatformOnly(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
            .isInstanceOfSatisfying(ServiceException.class, error ->
                assertThat(error.getMessageKey()).isEqualTo("sys.permission.metadata.platform.only"));
    }
}
