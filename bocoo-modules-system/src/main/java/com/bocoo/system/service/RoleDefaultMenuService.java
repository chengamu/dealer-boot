package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysRoleMenu;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.UserDefaultRouteVo;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleDefaultMenuService {

    private static final String VISIBLE = "1";
    private static final String ADMIN_DEFAULT_ROUTE = "/monitor/server";
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleMapper roleMapper;

    public void validateForSave(Long defaultMenuId, Long[] menuIds) {
        if (defaultMenuId == null) return;
        Set<Long> finalMenuIds = menuIds == null
            ? Collections.emptySet() : new HashSet<>(Arrays.asList(menuIds));
        if (!finalMenuIds.contains(defaultMenuId)) {
            throw ServiceException.ofMessageKey("sys.role.default.menu.not.authorized");
        }
        resolveMenu(defaultMenuId);
    }

    public UserDefaultRouteVo resolveForRole(SysRole role) {
        if (role == null || role.getDefaultMenuId() == null) return null;
        boolean authorized = roleMenuMapper.exists(Wrappers.<SysRoleMenu>lambdaQuery()
            .eq(SysRoleMenu::getRoleId, role.getRoleId())
            .eq(SysRoleMenu::getMenuId, role.getDefaultMenuId()));
        return authorized ? resolveMenuOrNull(role.getDefaultMenuId()) : null;
    }

    public UserDefaultRouteVo resolveAdminDefault() {
        List<SysMenu> menus = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
            .eq(SysMenu::getMenuType, UserConstants.TYPE_MENU)
            .eq(SysMenu::getVisible, VISIBLE)
            .eq(SysMenu::getStatus, UserConstants.MENU_NORMAL)
            .eq(SysMenu::getIsFrame, UserConstants.NO_FRAME));
        for (SysMenu menu : menus) {
            UserDefaultRouteVo route = resolveMenuOrNull(menu.getMenuId());
            if (route != null && ADMIN_DEFAULT_ROUTE.equals(route.getDefaultRoute())) return route;
        }
        return null;
    }

    public void enrichRoles(List<SysRoleVo> roles) {
        if (roles == null || roles.isEmpty()) return;
        List<Long> ids = roles.stream().map(SysRoleVo::getRoleId).toList();
        Map<Long, SysRole> entities = new HashMap<>();
        for (SysRole role : roleMapper.selectBatchIds(ids)) entities.put(role.getRoleId(), role);
        for (SysRoleVo role : roles) {
            SysRole entity = entities.get(role.getRoleId());
            if (entity == null) continue;
            role.setDefaultMenuId(entity.getDefaultMenuId());
            UserDefaultRouteVo route = entity.isAdmin() ? resolveAdminDefault() : resolveForRole(entity);
            role.setDefaultMenuName(route == null ? null : route.getDefaultRouteTitle());
        }
    }

    public UserDefaultRouteVo resolveMenuOrNull(Long menuId) {
        if (menuId == null) return null;
        try {
            return resolveMenu(menuId);
        } catch (ServiceException ignored) {
            return null;
        }
    }

    private UserDefaultRouteVo resolveMenu(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) throw ServiceException.ofMessageKey("sys.role.default.menu.not.found");
        if (!UserConstants.MENU_NORMAL.equals(menu.getStatus())) {
            throw ServiceException.ofMessageKey("sys.role.default.menu.disabled");
        }
        if (!UserConstants.TYPE_MENU.equals(menu.getMenuType()) || !VISIBLE.equals(menu.getVisible())) {
            throw ServiceException.ofMessageKey("sys.role.default.menu.not.page");
        }
        if (!UserConstants.NO_FRAME.equals(menu.getIsFrame()) || StringUtils.isBlank(menu.getComponent())) {
            throw ServiceException.ofMessageKey("sys.role.default.menu.route.invalid");
        }
        List<SysMenu> chain = menuChain(menu);
        String route = buildRoute(chain);
        String title = chain.stream().map(SysMenu::getMenuName).filter(StringUtils::isNotBlank)
            .reduce((left, right) -> left + " / " + right).orElse(menu.getMenuName());
        return new UserDefaultRouteVo(menuId, route, title);
    }

    private List<SysMenu> menuChain(SysMenu leaf) {
        List<SysMenu> chain = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        SysMenu current = leaf;
        while (current != null) {
            if (!visited.add(current.getMenuId())) {
                throw ServiceException.ofMessageKey("sys.role.default.menu.route.invalid");
            }
            chain.add(current);
            if (current.getParentId() == null || current.getParentId() == 0L) break;
            current = menuMapper.selectById(current.getParentId());
            if (current == null || !UserConstants.MENU_NORMAL.equals(current.getStatus())) {
                throw ServiceException.ofMessageKey("sys.role.default.menu.route.invalid");
            }
        }
        Collections.reverse(chain);
        return chain;
    }

    private String buildRoute(List<SysMenu> chain) {
        List<String> segments = new ArrayList<>();
        for (SysMenu menu : chain) {
            String rawPath = StringUtils.trim(menu.getPath());
            if (StringUtils.isBlank(rawPath) || rawPath.startsWith("//")
                || StringUtils.ishttp(rawPath) || rawPath.contains("..") || rawPath.contains(":")) {
                throw ServiceException.ofMessageKey("sys.role.default.menu.route.invalid");
            }
            String path = rawPath;
            while (path != null && path.startsWith("/")) path = path.substring(1);
            while (path != null && path.endsWith("/")) path = path.substring(0, path.length() - 1);
            if (StringUtils.isBlank(path) || path.contains("//")) {
                throw ServiceException.ofMessageKey("sys.role.default.menu.route.invalid");
            }
            segments.add(path);
        }
        return "/" + String.join("/", segments);
    }
}
