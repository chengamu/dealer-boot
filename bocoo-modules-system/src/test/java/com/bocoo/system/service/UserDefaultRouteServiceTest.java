package com.bocoo.system.service;

import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.UserDefaultRouteVo;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDefaultRouteServiceTest {

    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private RoleDefaultMenuService defaultMenuService;

    @Test
    void resolvesFirstValidRoleBySortThenId() {
        SysUserRole firstLink = link(100L, 3L);
        SysUserRole secondLink = link(100L, 2L);
        SysRole later = role(3L, 20, 30L);
        SysRole first = role(2L, 10, 20L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(firstLink, secondLink));
        when(roleMapper.selectBatchIds(List.of(3L, 2L))).thenReturn(List.of(later, first));
        when(defaultMenuService.resolveForRole(first))
            .thenReturn(new UserDefaultRouteVo(20L, "/sales/dashboard", "Sales / Dashboard"));

        UserDefaultRouteVo result = service().resolve(100L);

        assertThat(result.getDefaultMenuId()).isEqualTo(20L);
    }

    @Test
    void skipsInvalidEarlierRoleDefault() {
        SysRole first = role(2L, 10, 20L);
        SysRole second = role(3L, 20, 30L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(link(100L, 2L), link(100L, 3L)));
        when(roleMapper.selectBatchIds(List.of(2L, 3L))).thenReturn(List.of(first, second));
        when(defaultMenuService.resolveForRole(first)).thenReturn(null);
        when(defaultMenuService.resolveForRole(second))
            .thenReturn(new UserDefaultRouteVo(30L, "/production/queue", "Production / Queue"));

        assertThat(service().resolve(100L).getDefaultMenuId()).isEqualTo(30L);
    }

    private UserDefaultRouteService service() {
        return new UserDefaultRouteService(userRoleMapper, roleMapper, defaultMenuService);
    }

    private SysUserRole link(Long userId, Long roleId) {
        SysUserRole link = new SysUserRole();
        link.setUserId(userId);
        link.setRoleId(roleId);
        return link;
    }

    private SysRole role(Long roleId, int sort, Long defaultMenuId) {
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setRoleSort(sort);
        role.setDefaultMenuId(defaultMenuId);
        role.setStatus("1");
        role.setDelFlag("0");
        return role;
    }
}
