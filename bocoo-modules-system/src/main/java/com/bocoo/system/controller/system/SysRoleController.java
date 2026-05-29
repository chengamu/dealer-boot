package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysDeptBo;
import com.bocoo.system.domain.bo.SysRoleBo;
import com.bocoo.system.domain.bo.SysUserBo;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.service.SysDeptService;
import com.bocoo.system.service.SysRoleService;
import com.bocoo.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色信息
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/role")
@Tag(name = "角色管理", description = "角色信息管理接口")
public class SysRoleController extends BaseController {

    private final SysRoleService roleService;
    private final SysUserService userService;
    private final SysDeptService deptService;

    /**
     * 获取角色信息列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    @Operation(summary = "获取角色信息列表", description = "分页获取角色信息列表")
    public TableDataInfo<SysRoleVo> list(
            @Parameter(description = "角色查询参数")
            SysRoleBo role,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return roleService.selectPageRoleList(role, pageQuery);
    }

    /**
     * 导出角色信息列表
     */
    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:role:export")
    @PostMapping("/export")
    @Operation(summary = "导出角色信息列表", description = "导出角色信息列表到Excel")
    public void export(
            @Parameter(description = "角色查询参数")
            SysRoleBo role,
            HttpServletResponse response) {
        List<SysRoleVo> list = roleService.selectRoleList(role);
        ExcelUtil.exportExcel(list, "角色数据", SysRoleVo.class, response);
    }

    /**
     * 根据角色编号获取详细信息
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:role:query")
    @GetMapping(value = "/{roleId}")
    @Operation(summary = "根据角色编号获取详细信息", description = "根据角色ID获取角色详细信息")
    public R<SysRoleVo> getInfo(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        roleService.checkRoleDataScope(roleId);
        return R.ok(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增角色", description = "新增角色信息")
    public R<Void> add(
            @Parameter(description = "角色信息", required = true)
            @Validated @RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.fail("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        return toAjax(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改保存角色", description = "修改角色信息")
    public R<Void> edit(
            @Parameter(description = "角色信息", required = true)
            @Validated @RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.fail("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        if (roleService.updateRole(role) > 0) {
            return R.ok();
        }
        return R.fail("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 修改保存数据权限
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    @Operation(summary = "修改保存数据权限", description = "修改角色的数据权限")
    public R<Void> dataScope(
            @Parameter(description = "角色信息", required = true)
            @RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    @Operation(summary = "状态修改", description = "修改角色状态")
    public R<Void> changeStatus(
            @Parameter(description = "角色信息", required = true)
            @RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.updateRoleStatus(role.getRoleId(), role.getStatus()));
    }

    /**
     * 删除角色
     *
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:role:remove")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    @Operation(summary = "删除角色", description = "批量删除角色")
    public R<Void> remove(
            @Parameter(description = "角色ID数组", required = true)
            @PathVariable Long[] roleIds) {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * 获取角色选择框列表
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/optionselect")
    @Operation(summary = "获取角色选择框列表", description = "获取所有角色选择框列表")
    public R<List<SysRoleVo>> optionselect() {
        return R.ok(roleService.selectRoleAll());
    }

    /**
     * 查询已分配用户角色列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/allocatedList")
    @Operation(summary = "查询已分配用户角色列表", description = "分页查询已分配该角色的用户列表")
    public TableDataInfo<SysUserVo> allocatedList(
            @Parameter(description = "用户查询参数")
            SysUserBo user,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return userService.selectAllocatedList(user, pageQuery);
    }

    /**
     * 查询未分配用户角色列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    @Operation(summary = "查询未分配用户角色列表", description = "分页查询未分配该角色的用户列表")
    public TableDataInfo<SysUserVo> unallocatedList(
            @Parameter(description = "用户查询参数")
            SysUserBo user,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return userService.selectUnallocatedList(user, pageQuery);
    }

    /**
     * 取消授权用户
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    @Operation(summary = "取消授权用户", description = "取消用户的角色授权")
    public R<Void> cancelAuthUser(
            @Parameter(description = "用户角色关联信息", required = true)
            @RequestBody SysUserRole userRole) {
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID串
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    @Operation(summary = "批量取消授权用户", description = "批量取消用户的角色授权")
    public R<Void> cancelAuthUserAll(
            @Parameter(description = "角色ID", required = true)
            Long roleId,
            @Parameter(description = "用户ID数组", required = true)
            Long[] userIds) {
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     *
     * @param roleId  角色ID
     * @param userIds 用户ID串
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    @Operation(summary = "批量选择用户授权", description = "批量给用户授予角色")
    public R<Void> selectAuthUserAll(
            @Parameter(description = "角色ID", required = true)
            Long roleId,
            @Parameter(description = "用户ID数组", required = true)
            Long[] userIds) {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 获取对应角色部门树列表
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:role:list")
    @GetMapping(value = "/deptTree/{roleId}")
    @Operation(summary = "获取对应角色部门树列表", description = "根据角色ID获取部门树列表")
    public R<Map<String, Object>> roleDeptTreeselect(
            @Parameter(description = "角色ID", required = true)
            @PathVariable("roleId") Long roleId) {
        return R.ok(Map.of(
            "checkedKeys", deptService.selectDeptListByRoleId(roleId),
            "depts", deptService.selectDeptTreeList(new SysDeptBo())
        ));
    }
}
