package com.bocoo.system.service;

import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.mapper.SysPostMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysUserMapper;
import com.bocoo.system.mapper.SysUserPostMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysUserDataScopeTest {

    private static final long MERCHANT_TENANT_ID = 300001L;
    private static final long MERCHANT_USER_ID = 7001L;

    @Mock private SysUserMapper userMapper;
    @Mock private SysDeptMapper deptMapper;
    @Mock private SysRoleMapper roleMapper;
    @Mock private SysPostMapper postMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysUserPostMapper userPostMapper;
    @Mock private SysOssService ossService;
    @Mock private SysConfigService configService;
    @Mock private UserAvatarStorageService avatarStorageService;

    private SysUserService service;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(
            TenantType.PLATFORM.getCode(), TenantConstants.PLATFORM_TENANT_ID, 1L, "admin");
        TenantContextHolder.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        service = new SysUserService(userMapper, deptMapper, roleMapper, postMapper, userRoleMapper,
            userPostMapper, ossService, configService, avatarStorageService);
        when(userMapper.selectById(MERCHANT_USER_ID)).thenReturn(merchantUser());
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void platformContextRejectsMerchantUser() {
        assertThatThrownBy(() -> service.checkUserDataScope(MERCHANT_USER_ID))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void controlledMerchantTenantContextAllowsMerchantUser() {
        assertThatCode(() -> TenantContextHolder.callWithTenant(MERCHANT_TENANT_ID, () -> {
            service.checkUserDataScope(MERCHANT_USER_ID);
            return null;
        })).doesNotThrowAnyException();
    }

    private SysUser merchantUser() {
        SysUser user = new SysUser();
        user.setUserId(MERCHANT_USER_ID);
        user.setTenantId(MERCHANT_TENANT_ID);
        return user;
    }
}
