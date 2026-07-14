package com.bocoo.system.service;

import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantAccountDefaultsServiceTest {

    @Mock
    private SysDeptMapper deptMapper;
    @Mock
    private SysRoleMapper roleMapper;

    @Test
    void ensureDefaultsReusesPlatformRoles() {
        AtomicLong deptId = new AtomicLong(10L);
        when(deptMapper.insert(any())).thenAnswer(invocation -> {
            invocation.<SysDept>getArgument(0).setDeptId(deptId.getAndIncrement());
            return 1;
        });
        SysRole owner = role(2L, MerchantAccountDefaultsService.OWNER_ROLE_KEY);
        SysRole employee = role(4L, MerchantAccountDefaultsService.EMPLOYEE_ROLE_KEY);
        when(roleMapper.selectOne(any(), eq(false))).thenReturn(owner, employee);

        MerchantAccountDefaultsService service = service();
        MerchantAccountDefaultsService.MerchantDefaults defaults = service.ensureDefaults();

        verify(roleMapper, times(2)).selectOne(any(), eq(false));
        verify(roleMapper, never()).insert(any());
        verify(roleMapper, never()).updateById(any());
        assertThat(defaults.ownerRole()).isSameAs(owner);
        assertThat(defaults.employeeRole()).isSameAs(employee);
        assertThat(defaults.defaultDept().getDeptName())
            .isEqualTo(MerchantAccountDefaultsService.DEFAULT_DEPT_NAME);
    }

    @Test
    void ensureEmployeeRoleRejectsMissingPlatformRole() {
        when(roleMapper.selectOne(any(), eq(false))).thenReturn(null);

        assertThatThrownBy(() -> service().ensureEmployeeRole())
            .hasMessageContaining(MerchantAccountDefaultsService.EMPLOYEE_ROLE_KEY);
    }

    private MerchantAccountDefaultsService service() {
        return new MerchantAccountDefaultsService(deptMapper, roleMapper);
    }

    private SysRole role(Long roleId, String roleKey) {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setRoleKey(roleKey);
        return role;
    }
}
