package com.bocoo.system.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.system.domain.bo.SysRoleBo;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Role facade. Query and relation details live in focused collaborators.
 */
@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleQueryService queryService;
    private final SysRoleRelationService relationService;
    private final RoleDefaultMenuService defaultMenuService;

    public TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo role, PageQuery pageQuery) {
        return queryService.selectPageRoleList(role, pageQuery);
    }

    public List<SysRoleVo> selectRoleList(SysRoleBo role) {
        return queryService.selectRoleList(role);
    }

    public List<SysRoleVo> selectRolesByUserId(Long userId) {
        return queryService.selectRolesByUserId(userId);
    }

    public Set<String> selectRolePermissionByUserId(Long userId) {
        return queryService.selectRolePermissionByUserId(userId);
    }

    public List<SysRoleVo> selectRoleAll() {
        return queryService.selectRoleAll();
    }

    public List<Long> selectRoleListByUserId(Long userId) {
        return queryService.selectRoleListByUserId(userId);
    }

    public SysRoleVo selectRoleById(Long roleId) {
        return queryService.selectRoleById(roleId);
    }

    public boolean checkRoleNameUnique(SysRoleBo role) {
        return queryService.checkRoleNameUnique(role);
    }

    public boolean checkRoleKeyUnique(SysRoleBo role) {
        return queryService.checkRoleKeyUnique(role);
    }

    public void checkRoleAllowed(SysRoleBo role) {
        queryService.checkRoleAllowed(role);
    }

    public void checkRoleDataScope(Long roleId) {
        queryService.checkRoleDataScope(roleId);
    }

    public long countUserRoleByRoleId(Long roleId) {
        return queryService.countUserRoleByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRoleBo bo) {
        defaultMenuService.validateForSave(bo.getDefaultMenuId(), bo.getMenuIds());
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        roleMapper.insert(role);
        if (role == null) return 0;
        bo.setRoleId(role.getRoleId());
        return relationService.insertMenus(role.getRoleId(), bo.getMenuIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRoleBo bo) {
        defaultMenuService.validateForSave(bo.getDefaultMenuId(), bo.getMenuIds());
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        int rows = roleMapper.updateById(role);
        if (rows <= 0 || role == null) return rows;
        return relationService.replaceMenus(role.getRoleId(), bo.getMenuIds());
    }

    public int updateRoleStatus(Long roleId, String status) {
        if (UserConstants.ROLE_DISABLE.equals(status) && countUserRoleByRoleId(roleId) > 0) {
            throw ServiceException.ofMessageKey("sys.role.assigned.disable.denied");
        }
        return roleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
            .set(SysRole::getStatus, status).eq(SysRole::getRoleId, roleId));
    }

    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRoleBo bo) {
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        int rows = roleMapper.updateById(role);
        if (rows <= 0 || role == null) return rows;
        return relationService.replaceDepartments(role);
    }

    public int insertRoleMenu(SysRoleBo role) {
        return relationService.insertMenus(role.getRoleId(), role.getMenuIds());
    }

    public int insertRoleDept(SysRole role) {
        return relationService.insertDepartments(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Long roleId) {
        relationService.deleteRelations(roleId);
        return roleMapper.deleteById(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            SysRole role = roleMapper.selectById(roleId);
            checkRoleAllowed(BeanUtil.toBean(role, SysRoleBo.class));
            checkRoleDataScope(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw ServiceException.ofMessageKey("sys.role.assigned.delete.denied", role.getRoleName());
            }
        }
        List<Long> ids = Arrays.asList(roleIds);
        relationService.deleteRelations(ids);
        return roleMapper.deleteBatchIds(ids);
    }

    public int deleteAuthUser(SysUserRole userRole) {
        return relationService.deleteAuthUser(userRole);
    }

    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        return relationService.deleteAuthUsers(roleId, userIds);
    }

    public int insertAuthUsers(Long roleId, Long[] userIds) {
        return relationService.insertAuthUsers(roleId, userIds);
    }

    public void cleanOnlineUserByRole(Long roleId) {
        relationService.cleanOnlineUserByRole(roleId);
    }

    public List<SysRoleVo> selectRolesAuthByUserId(Long userId) {
        return queryService.selectRolesAuthByUserId(userId);
    }
}
