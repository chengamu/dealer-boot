package com.bocoo.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.constant.CacheConstants;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysRoleDept;
import com.bocoo.system.domain.entity.SysRoleMenu;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.mapper.SysRoleDeptMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleRelationService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PlatformPermissionMetadataGuard permissionMetadataGuard;

    public int insertMenus(Long roleId, Long[] menuIds) {
        permissionMetadataGuard.requirePlatformTenant();
        List<SysRoleMenu> rows = StreamUtils.toList(
            menuIds == null ? List.<Long>of() : Arrays.asList(menuIds), menuId -> {
                SysRoleMenu row = new SysRoleMenu();
                row.setRoleId(roleId);
                row.setMenuId(menuId);
                row.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
                return row;
            });
        return rows.isEmpty() ? 1 : (roleMenuMapper.insertBatch(rows) ? rows.size() : 0);
    }

    public int replaceMenus(Long roleId, Long[] menuIds) {
        permissionMetadataGuard.requirePlatformTenant();
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
            .eq(SysRoleMenu::getTenantId, TenantConstants.PLATFORM_TENANT_ID)
            .eq(SysRoleMenu::getRoleId, roleId));
        return insertMenus(roleId, menuIds);
    }

    public int replaceDepartments(SysRole role) {
        permissionMetadataGuard.requirePlatformTenant();
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, role.getRoleId()));
        return insertDepartments(role);
    }

    public int insertDepartments(SysRole role) {
        permissionMetadataGuard.requirePlatformTenant();
        Long[] deptIds = role.getDeptIds();
        List<SysRoleDept> rows = StreamUtils.toList(
            deptIds == null ? List.<Long>of() : Arrays.asList(deptIds), deptId -> {
                SysRoleDept row = new SysRoleDept();
                row.setRoleId(role.getRoleId());
                row.setDeptId(deptId);
                return row;
            });
        return rows.isEmpty() ? 1 : (roleDeptMapper.insertBatch(rows) ? rows.size() : 0);
    }

    public void deleteRelations(Long roleId) {
        permissionMetadataGuard.requirePlatformTenant();
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
            .eq(SysRoleMenu::getTenantId, TenantConstants.PLATFORM_TENANT_ID)
            .eq(SysRoleMenu::getRoleId, roleId));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
    }

    public void deleteRelations(Collection<Long> roleIds) {
        permissionMetadataGuard.requirePlatformTenant();
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
            .eq(SysRoleMenu::getTenantId, TenantConstants.PLATFORM_TENANT_ID)
            .in(SysRoleMenu::getRoleId, roleIds));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds));
    }

    public int deleteAuthUser(SysUserRole userRole) {
        permissionMetadataGuard.requirePlatformTenant();
        int rows = userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, userRole.getRoleId())
            .eq(SysUserRole::getUserId, userRole.getUserId()));
        if (rows > 0) cleanOnlineUserByRole(userRole.getRoleId());
        return rows;
    }

    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        permissionMetadataGuard.requirePlatformTenant();
        int rows = userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, roleId).in(SysUserRole::getUserId, Arrays.asList(userIds)));
        if (rows > 0) cleanOnlineUserByRole(roleId);
        return rows;
    }

    public int insertAuthUsers(Long roleId, Long[] userIds) {
        permissionMetadataGuard.requirePlatformTenant();
        List<SysUserRole> rows = StreamUtils.toList(Arrays.asList(userIds), userId -> {
            SysUserRole row = new SysUserRole();
            row.setUserId(userId);
            row.setRoleId(roleId);
            return row;
        });
        int result = CollUtil.isEmpty(rows) ? 1 : (userRoleMapper.insertBatch(rows) ? rows.size() : 0);
        if (result > 0) cleanOnlineUserByRole(roleId);
        return result;
    }

    public void cleanOnlineUserByRole(Long roleId) {
        permissionMetadataGuard.requirePlatformTenant();
        long assignedUsers = TenantContextHolder.callWithIgnore(() -> userRoleMapper.selectCount(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)));
        if (assignedUsers == 0) return;
        List<String> keys = StpUtil.searchTokenValue("", 0, CacheConstants.ONLINE_TOKEN_SCAN_LIMIT, false);
        if (CollUtil.isEmpty(keys)) return;
        keys.parallelStream().forEach(key -> logoutRoleUser(key, roleId));
    }

    private void logoutRoleUser(String key, Long roleId) {
        String token = StringUtils.substringAfterLast(key, ":");
        if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) return;
        LoginUser loginUser = LoginHelper.getLoginUser(token);
        if (ObjectUtil.isNull(loginUser)
            || loginUser.getRoles().stream().noneMatch(role -> role.getRoleId().equals(roleId))) return;
        try {
            StpUtil.logoutByTokenValue(token);
        } catch (NotLoginException ignored) {
            log.debug("Skip role online-user cleanup because token is not logged in.");
        }
    }
}
