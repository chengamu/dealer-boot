package com.bocoo.system.service;

import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.UserDefaultRouteVo;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDefaultRouteService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final RoleDefaultMenuService defaultMenuService;

    public UserDefaultRouteVo resolve(Long userId) {
        if (LoginHelper.isAdmin(userId)) return defaultMenuService.resolveAdminDefault();
        List<Long> roleIds = userRoleMapper.selectList(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId))
            .stream().map(SysUserRole::getRoleId).toList();
        if (roleIds == null || roleIds.isEmpty()) return null;
        return roleMapper.selectBatchIds(roleIds).stream()
            .filter(role -> UserConstants.ROLE_NORMAL.equals(role.getStatus()))
            .filter(role -> UserConstants.NOT_DELETED.equals(role.getDelFlag()))
            .filter(role -> role.getDefaultMenuId() != null)
            .sorted(Comparator.comparing(SysRole::getRoleSort).thenComparing(SysRole::getRoleId))
            .map(defaultMenuService::resolveForRole)
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
