package com.bocoo.system.service;

import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleDefaultMenuServiceTest {

    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;
    @Mock
    private SysRoleMapper roleMapper;

    @Test
    void emptyDefaultMenuIsAllowed() {
        RoleDefaultMenuService service = service();

        assertThatCode(() -> service.validateForSave(null, new Long[]{10L})).doesNotThrowAnyException();
    }

    @Test
    void defaultMenuMustBelongToSubmittedRoleMenus() {
        RoleDefaultMenuService service = service();

        assertThatThrownBy(() -> service.validateForSave(20L, new Long[]{10L}))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void validPageMenuResolvesAbsoluteRouteAndTitle() {
        SysMenu parent = menu(10L, 0L, "monitor", "System Monitor", "M");
        SysMenu leaf = menu(20L, 10L, "server", "Server Resource", "C");
        leaf.setComponent("monitor/server/index");
        when(menuMapper.selectOne(any(), eq(false))).thenReturn(leaf, parent, leaf, parent);

        RoleDefaultMenuService service = service();
        service.validateForSave(20L, new Long[]{10L, 20L});

        var route = service.resolveMenuOrNull(20L);
        assertThat(route.getDefaultRoute()).isEqualTo("/monitor/server");
        assertThat(route.getDefaultRouteTitle()).isEqualTo("System Monitor / Server Resource");
    }

    @Test
    void disabledPageMenuIsRejected() {
        SysMenu leaf = menu(20L, 0L, "server", "Server Resource", "C");
        leaf.setComponent("monitor/server/index");
        leaf.setStatus("0");
        when(menuMapper.selectOne(any(), eq(false))).thenReturn(leaf);

        assertThatThrownBy(() -> service().validateForSave(20L, new Long[]{20L}))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void protocolRelativeRouteIsRejected() {
        SysMenu leaf = menu(20L, 0L, "//external.example", "External", "C");
        leaf.setComponent("external/index");
        when(menuMapper.selectOne(any(), eq(false))).thenReturn(leaf);

        assertThatThrownBy(() -> service().validateForSave(20L, new Long[]{20L}))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void roleRouteRequiresCurrentRoleMenuRelation() {
        SysRole role = new SysRole();
        role.setRoleId(2L);
        role.setDefaultMenuId(20L);
        when(roleMenuMapper.exists(any())).thenReturn(false);

        assertThat(service().resolveForRole(role)).isNull();
    }

    private RoleDefaultMenuService service() {
        return new RoleDefaultMenuService(menuMapper, roleMenuMapper, roleMapper);
    }

    private SysMenu menu(Long id, Long parentId, String path, String name, String type) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(id);
        menu.setParentId(parentId);
        menu.setPath(path);
        menu.setMenuName(name);
        menu.setMenuType(type);
        menu.setVisible("1");
        menu.setStatus("1");
        menu.setIsFrame("1");
        return menu;
    }
}
