package com.bocoo.system.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.bo.SysRoleBo;
import com.bocoo.system.mapper.SysRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleQueryService queryService;
    @Mock
    private SysRoleRelationService relationService;
    @Mock
    private RoleDefaultMenuService defaultMenuService;
    @Mock
    private PlatformPermissionMetadataGuard permissionMetadataGuard;

    @Test
    void insertValidatesDefaultMenuBeforeWritingRole() {
        SysRoleBo bo = new SysRoleBo();
        bo.setDefaultMenuId(20L);
        bo.setMenuIds(new Long[]{10L});
        doThrow(ServiceException.ofMessageKey("sys.role.default.menu.not.authorized"))
            .when(defaultMenuService).validateForSave(20L, bo.getMenuIds());

        SysRoleService service = new SysRoleService(
            roleMapper, queryService, relationService, defaultMenuService, permissionMetadataGuard);

        assertThatThrownBy(() -> service.insertRole(bo)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(roleMapper, relationService);
    }
}
