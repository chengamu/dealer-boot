package com.bocoo.system.service;

import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.mybatis.enums.DataScopeType;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantAccountDefaultsServiceTest {

    @Mock
    private SysDeptMapper deptMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @Test
    void ensureDefaultsCreatesOnlyOwnerAndEmployeeRoles() {
        AtomicLong deptId = new AtomicLong(10L);
        AtomicLong roleId = new AtomicLong(20L);
        AtomicLong menuId = new AtomicLong(100L);
        when(deptMapper.insert(any())).thenAnswer(invocation -> {
            invocation.<SysDept>getArgument(0).setDeptId(deptId.getAndIncrement());
            return 1;
        });
        when(roleMapper.insert(any())).thenAnswer(invocation -> {
            invocation.<SysRole>getArgument(0).setRoleId(roleId.getAndIncrement());
            return 1;
        });
        when(menuMapper.insert(any())).thenAnswer(invocation -> {
            invocation.<SysMenu>getArgument(0).setMenuId(menuId.getAndIncrement());
            return 1;
        });
        when(roleMenuMapper.exists(any())).thenReturn(false);

        MerchantAccountDefaultsService service = service();
        MerchantAccountDefaultsService.MerchantDefaults defaults = service.ensureDefaults();

        ArgumentCaptor<SysRole> roles = ArgumentCaptor.forClass(SysRole.class);
        verify(roleMapper, times(2)).insert(roles.capture());
        assertThat(roles.getAllValues()).extracting(SysRole::getRoleKey)
            .containsExactly(MerchantAccountDefaultsService.OWNER_ROLE_KEY,
                MerchantAccountDefaultsService.EMPLOYEE_ROLE_KEY);
        assertRole(defaults.ownerRole(), "店主", DataScopeType.DEPT_AND_CHILD.getCode());
        assertRole(defaults.employeeRole(), "营业员", DataScopeType.SELF.getCode());
        assertThat(defaults.defaultDept().getDeptName())
            .isEqualTo(MerchantAccountDefaultsService.DEFAULT_DEPT_NAME);
    }

    @Test
    void ensureEmployeeRoleRepairsDisabledLegacyRole() {
        SysRole legacy = new SysRole();
        legacy.setRoleId(30L);
        legacy.setRoleName("Merchant Employee");
        legacy.setRoleKey(MerchantAccountDefaultsService.EMPLOYEE_ROLE_KEY);
        legacy.setRoleSort(3);
        legacy.setDataScope(DataScopeType.ALL.getCode());
        legacy.setStatus(UserStatus.DISABLE.getCode());
        legacy.setDelFlag("1");
        when(roleMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(legacy);

        SysRole repaired = service().ensureEmployeeRole();

        verify(roleMapper).updateById(legacy);
        assertRole(repaired, "营业员", DataScopeType.SELF.getCode());
    }

    private MerchantAccountDefaultsService service() {
        return new MerchantAccountDefaultsService(deptMapper, roleMapper, menuMapper, roleMenuMapper);
    }

    private void assertRole(SysRole role, String roleName, String dataScope) {
        assertThat(role.getRoleName()).isEqualTo(roleName);
        assertThat(role.getDataScope()).isEqualTo(dataScope);
        assertThat(role.getStatus()).isEqualTo(UserStatus.OK.getCode());
        assertThat(role.getDelFlag()).isEqualTo(UserConstants.NOT_DELETED);
    }
}
