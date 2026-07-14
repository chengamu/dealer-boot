package com.bocoo.system.service;

import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.system.domain.bo.SysMenuBo;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysMenuCommandService {

    private final SysMenuMapper menuMapper;
    private final PlatformPermissionMetadataGuard permissionMetadataGuard;

    public int insertMenu(SysMenuBo bo) {
        permissionMetadataGuard.requirePlatformTenant();
        SysMenu menu = MapstructUtils.convert(bo, SysMenu.class);
        menu.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        return menuMapper.insert(menu);
    }

    public int updateMenu(SysMenuBo bo) {
        permissionMetadataGuard.requirePlatformTenant();
        SysMenu menu = MapstructUtils.convert(bo, SysMenu.class);
        menu.setTenantId(TenantConstants.PLATFORM_TENANT_ID);
        return menuMapper.updateById(menu);
    }

    public int deleteMenuById(Long menuId) {
        permissionMetadataGuard.requirePlatformTenant();
        return menuMapper.deleteById(menuId);
    }
}
