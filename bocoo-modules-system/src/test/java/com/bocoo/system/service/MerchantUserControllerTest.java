package com.bocoo.system.service;

import cn.hutool.extra.spring.SpringUtil;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.service.I18nService;
import com.bocoo.system.controller.system.MerchantUserController;
import com.bocoo.system.domain.bo.SysUserBo;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.mapper.SysTenantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MerchantUserControllerTest {

    @BeforeAll
    static void installI18nService() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("i18nService", mock(I18nService.class));
        context.refresh();
        new SpringUtil().setApplicationContext(context);
    }

    @Mock
    private SysUserService userService;
    @Mock
    private MerchantAccountDefaultsService merchantDefaultsService;
    @Mock
    private SysTenantMapper tenantMapper;

    private MerchantUserController controller;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7001L, "owner");
        controller = new MerchantUserController(userService, merchantDefaultsService, tenantMapper);
    }

    @Test
    void addIgnoresClientTenantAndAssignsEmployeeDefaults() {
        SysDept defaultDept = new SysDept();
        defaultDept.setDeptId(11L);
        SysRole employeeRole = new SysRole();
        employeeRole.setRoleId(22L);
        when(merchantDefaultsService.ensureDefaultDept()).thenReturn(defaultDept);
        when(merchantDefaultsService.ensureEmployeeRole()).thenReturn(employeeRole);
        when(userService.checkUserNameUnique(any())).thenReturn(true);
        when(userService.resolveInitialPassword("Initial9!")).thenReturn("Initial9!");
        when(userService.insertUser(any())).thenReturn(1);

        SysUserBo request = new SysUserBo();
        request.setTenantId(999999L);
        request.setUserName("employee@example.com");
        request.setNickName("Employee");
        request.setPassword("Initial9!");

        controller.add(request);

        ArgumentCaptor<SysUserBo> saved = ArgumentCaptor.forClass(SysUserBo.class);
        verify(userService).insertUser(saved.capture());
        assertThat(saved.getValue().getTenantId()).isEqualTo(300001L);
        assertThat(saved.getValue().getDeptId()).isEqualTo(11L);
        assertThat(saved.getValue().getRoleIds()).containsExactly(22L);
    }

    @Test
    void detailDoesNotExposeDefaultDepartmentOrRoleOptions() {
        SysRoleVo role = new SysRoleVo();
        role.setRoleId(22L);
        role.setRoleKey(MerchantAccountDefaultsService.EMPLOYEE_ROLE_KEY);
        SysUserVo user = new SysUserVo();
        user.setUserId(42L);
        user.setDeptId(11L);
        user.setDeptName("Dealer");
        user.setRoles(List.of(role));
        user.setRoleIds(new Long[] {22L});
        user.setRoleId(22L);
        user.setPostIds(new Long[] {31L});
        when(userService.selectUserById(42L)).thenReturn(user);

        R<Map<String, Object>> response = controller.getInfo(42L, null);

        assertThat(response.getData()).doesNotContainKeys("roles", "roleIds", "posts", "postIds");
        SysUserVo detail = (SysUserVo) response.getData().get("user");
        assertThat(detail.getDeptId()).isNull();
        assertThat(detail.getDeptName()).isNull();
        assertThat(detail.getRoles()).isNull();
        assertThat(detail.getRoleIds()).isNull();
        assertThat(detail.getRoleId()).isNull();
        assertThat(detail.getPostIds()).isNull();
        verify(userService).checkUserDataScope(42L);
    }
}
