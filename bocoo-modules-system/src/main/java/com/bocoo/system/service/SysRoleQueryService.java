package com.bocoo.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.SysRoleBo;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysRoleQueryService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final RoleDefaultMenuService defaultMenuService;

    public TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo role, PageQuery pageQuery) {
        Page<SysRoleVo> page = roleMapper.selectPageRoleList(pageQuery.build(), buildQueryWrapper(role));
        defaultMenuService.enrichRoles(page.getRecords());
        return TableDataInfo.build(page);
    }

    public List<SysRoleVo> selectRoleList(SysRoleBo role) {
        List<SysRoleVo> roles = roleMapper.selectRoleList(buildQueryWrapper(role));
        defaultMenuService.enrichRoles(roles);
        return roles;
    }

    public List<SysRoleVo> selectRolesByUserId(Long userId) {
        List<SysRoleVo> roles = roleMapper.selectRolesByUserId(userId);
        defaultMenuService.enrichRoles(roles);
        return roles;
    }

    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRoleVo> roles = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permissions = new HashSet<>();
        for (SysRoleVo role : roles) {
            if (role != null) permissions.addAll(StringUtils.splitList(role.getRoleKey().trim()));
        }
        return permissions;
    }

    public List<SysRoleVo> selectRoleAll() {
        return selectRoleList(new SysRoleBo());
    }

    public List<Long> selectRoleListByUserId(Long userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    public SysRoleVo selectRoleById(Long roleId) {
        SysRoleVo role = roleMapper.selectRoleById(roleId);
        if (role != null) defaultMenuService.enrichRoles(List.of(role));
        return role;
    }

    public boolean checkRoleNameUnique(SysRoleBo role) {
        return !roleMapper.exists(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getTenantId, TenantConstants.PLATFORM_TENANT_ID)
            .eq(SysRole::getRoleName, role.getRoleName())
            .ne(role.getRoleId() != null, SysRole::getRoleId, role.getRoleId()));
    }

    public boolean checkRoleKeyUnique(SysRoleBo role) {
        return !roleMapper.exists(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getTenantId, TenantConstants.PLATFORM_TENANT_ID)
            .eq(SysRole::getRoleKey, role.getRoleKey())
            .ne(role.getRoleId() != null, SysRole::getRoleId, role.getRoleId()));
    }

    public void checkRoleAllowed(SysRoleBo role) {
        if (role.getRoleId() != null && role.isAdmin()) {
            throw ServiceException.ofMessageKey("sys.role.admin.operation.denied");
        }
        if (role.getRoleId() == null && StringUtils.equals(role.getRoleKey(), UserConstants.ADMIN_ROLE_KEY)) {
            throw ServiceException.ofMessageKey("sys.role.admin.key.use.denied");
        }
        if (role.getRoleId() == null) return;
        SysRole current = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleId, role.getRoleId())
            .eq(SysRole::getTenantId, TenantConstants.PLATFORM_TENANT_ID), false);
        if (current == null || StringUtils.equals(current.getRoleKey(), role.getRoleKey())) return;
        if (StringUtils.equals(current.getRoleKey(), UserConstants.ADMIN_ROLE_KEY)) {
            throw ServiceException.ofMessageKey("sys.role.admin.key.update.denied");
        }
        if (StringUtils.equals(role.getRoleKey(), UserConstants.ADMIN_ROLE_KEY)) {
            throw ServiceException.ofMessageKey("sys.role.admin.key.use.denied");
        }
    }

    public void checkRoleDataScope(Long roleId) {
        if (LoginHelper.isAdmin()) return;
        SysRoleBo role = new SysRoleBo();
        role.setRoleId(roleId);
        if (CollUtil.isEmpty(selectRoleList(role))) {
            throw ServiceException.ofMessageKey("sys.role.data.permission.denied");
        }
    }

    public long countUserRoleByRoleId(Long roleId) {
        return TenantContextHolder.callWithIgnore(() -> userRoleMapper.selectCount(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)));
    }

    public List<SysRoleVo> selectRolesAuthByUserId(Long userId) {
        List<SysRoleVo> userRoles = roleMapper.selectRolesByUserId(userId);
        List<SysRoleVo> roles = roleMapper.selectVoList(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getTenantId, TenantConstants.PLATFORM_TENANT_ID));
        Set<Long> userRoleIds = StreamUtils.toSet(userRoles, SysRoleVo::getRoleId);
        for (SysRoleVo role : roles) role.setFlag(userRoleIds.contains(role.getRoleId()));
        defaultMenuService.enrichRoles(roles);
        return roles;
    }

    private Wrapper<SysRole> buildQueryWrapper(SysRoleBo role) {
        Map<String, Object> params = role.getParams();
        QueryWrapper<SysRole> wrapper = Wrappers.query();
        return wrapper.eq("r.tenant_id", TenantConstants.PLATFORM_TENANT_ID)
            .eq("r.del_flag", UserConstants.NOT_DELETED)
            .eq(ObjectUtil.isNotNull(role.getRoleId()), "r.role_id", role.getRoleId())
            .like(StringUtils.isNotBlank(role.getRoleName()), "r.role_name", role.getRoleName())
            .eq(StringUtils.isNotBlank(role.getStatus()), "r.status", role.getStatus())
            .like(StringUtils.isNotBlank(role.getRoleKey()), "r.role_key", role.getRoleKey())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "r.create_time", params.get("beginTime"), params.get("endTime"))
            .orderByAsc("r.role_sort").orderByAsc("r.create_time");
    }
}
