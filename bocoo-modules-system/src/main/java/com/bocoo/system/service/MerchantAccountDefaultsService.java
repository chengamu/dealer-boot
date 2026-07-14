package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MerchantAccountDefaultsService {

    public static final String DEFAULT_DEPT_NAME = "Dealer";
    public static final String OWNER_ROLE_KEY = "merchant_admin";
    public static final String EMPLOYEE_ROLE_KEY = "merchant_employee";

    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;

    @Transactional(rollbackFor = Exception.class)
    public MerchantDefaults ensureDefaults() {
        SysDept defaultDept = ensureDept(DEFAULT_DEPT_NAME, 1);
        SysRole ownerRole = requirePlatformRole(OWNER_ROLE_KEY);
        SysRole employeeRole = requirePlatformRole(EMPLOYEE_ROLE_KEY);
        return new MerchantDefaults(defaultDept, ownerRole, employeeRole);
    }

    public SysDept ensureDefaultDept() {
        return ensureDept(DEFAULT_DEPT_NAME, 1);
    }

    public SysRole ensureOwnerRole() {
        return requirePlatformRole(OWNER_ROLE_KEY);
    }

    public SysRole ensureEmployeeRole() {
        return requirePlatformRole(EMPLOYEE_ROLE_KEY);
    }

    private SysDept ensureDept(String deptName, Integer orderNum) {
        SysDept dept = deptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getDeptName, deptName), false);
        if (dept != null) {
            return dept;
        }
        dept = new SysDept();
        dept.setParentId(0L);
        dept.setAncestors("0");
        dept.setDeptName(deptName);
        dept.setOrderNum(orderNum);
        dept.setStatus(UserConstants.DEPT_NORMAL);
        dept.setDelFlag(UserConstants.NOT_DELETED);
        deptMapper.insert(dept);
        return dept;
    }

    private SysRole requirePlatformRole(String roleKey) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getTenantId, TenantConstants.PLATFORM_TENANT_ID)
            .eq(SysRole::getRoleKey, roleKey)
            .eq(SysRole::getStatus, UserConstants.ROLE_NORMAL)
            .eq(SysRole::getDelFlag, UserConstants.NOT_DELETED), false);
        if (role == null) {
            throw new ServiceException("平台固定商户角色未初始化: " + roleKey);
        }
        return role;
    }

    public record MerchantDefaults(SysDept defaultDept, SysRole ownerRole, SysRole employeeRole) {
    }
}
