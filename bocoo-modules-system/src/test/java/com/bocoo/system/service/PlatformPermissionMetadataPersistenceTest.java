package com.bocoo.system.service;

import cn.hutool.extra.spring.SpringUtil;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.system.domain.bo.SysMenuBo;
import com.bocoo.system.domain.bo.SysRoleBo;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysRoleMenu;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysRoleDeptMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import io.github.linpeilie.Converter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class PlatformPermissionMetadataPersistenceTest {

    private static Converter converter;

    @Mock private SysMenuMapper menuMapper;
    @Mock private SysRoleMapper roleMapper;
    @Mock private SysRoleMenuMapper roleMenuMapper;
    @Mock private SysRoleDeptMapper roleDeptMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysRoleQueryService roleQueryService;
    @Mock private RoleDefaultMenuService defaultMenuService;

    private PlatformPermissionMetadataGuard guard;

    @BeforeAll
    static void installMapstructConverter() {
        converter = mock(Converter.class);
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("mapstructConverter", converter);
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @BeforeEach
    void setUp() {
        reset(converter);
        when(converter.convert(any(Object.class), any(Class.class))).thenAnswer(invocation ->
            invocation.<Class<?>>getArgument(1).getDeclaredConstructor().newInstance());
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(
            TenantType.PLATFORM.getCode(), TenantConstants.PLATFORM_TENANT_ID, 1L, "admin");
        guard = new PlatformPermissionMetadataGuard();
    }

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void platformMenuInsertForcesPlatformTenantId() {
        when(menuMapper.insert(any(SysMenu.class))).thenReturn(1);
        SysMenuCommandService service = new SysMenuCommandService(menuMapper, guard);

        assertThat(service.insertMenu(new SysMenuBo())).isEqualTo(1);

        ArgumentCaptor<SysMenu> captor = ArgumentCaptor.forClass(SysMenu.class);
        verify(menuMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TenantConstants.PLATFORM_TENANT_ID);
    }

    @Test
    void platformRoleInsertForcesPlatformTenantId() {
        when(roleMapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            invocation.<SysRole>getArgument(0).setRoleId(20L);
            return 1;
        });
        when(roleMenuMapper.insertBatch(any(Collection.class))).thenReturn(true);
        SysRoleRelationService relationService = new SysRoleRelationService(
            roleMenuMapper, roleDeptMapper, userRoleMapper, guard);
        SysRoleService service = new SysRoleService(
            roleMapper, roleQueryService, relationService, defaultMenuService, guard);
        SysRoleBo bo = new SysRoleBo();
        bo.setMenuIds(new Long[]{30L});

        assertThat(service.insertRole(bo)).isEqualTo(1);

        ArgumentCaptor<SysRole> role = ArgumentCaptor.forClass(SysRole.class);
        verify(roleMapper).insert(role.capture());
        assertThat(role.getValue().getTenantId()).isEqualTo(TenantConstants.PLATFORM_TENANT_ID);
        ArgumentCaptor<Collection<SysRoleMenu>> relations = ArgumentCaptor.forClass(Collection.class);
        verify(roleMenuMapper).insertBatch(relations.capture());
        assertThat(relations.getValue()).allSatisfy(row ->
            assertThat(row.getTenantId()).isEqualTo(TenantConstants.PLATFORM_TENANT_ID));
    }

    @Test
    void globalRoleAssignmentCountUsesControlledCrossTenantScope() {
        TenantContextHolder.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        when(userRoleMapper.selectCount(any())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.isIgnore()).isTrue();
            return 3L;
        });
        SysRoleQueryService service = new SysRoleQueryService(
            roleMapper, userRoleMapper, defaultMenuService);

        assertThat(service.countUserRoleByRoleId(20L)).isEqualTo(3L);
        assertThat(TenantContextHolder.getRequiredTenantId())
            .isEqualTo(TenantConstants.PLATFORM_TENANT_ID);
        assertThat(TenantContextHolder.isIgnore()).isFalse();
    }
}
