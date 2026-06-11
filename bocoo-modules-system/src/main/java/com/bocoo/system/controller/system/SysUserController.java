package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bocoo.system.service.SysDeptService;
import com.bocoo.system.service.SysPostService;
import com.bocoo.system.service.SysRoleService;
import com.bocoo.system.service.SysUserService;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.excel.core.ExcelResult;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.idempotent.annotation.RepeatSubmit;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.ratelimiter.annotation.RateLimiter;
import com.bocoo.common.ratelimiter.enums.LimitType;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysDeptBo;
import com.bocoo.system.domain.bo.SysPostBo;
import com.bocoo.system.domain.bo.SysRoleBo;
import com.bocoo.system.domain.bo.SysUserBo;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.SysUserExportVo;
import com.bocoo.system.domain.vo.SysUserImportVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.listener.SysUserImportListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户信息
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
@Tag(name = "用户管理", description = "用户信息管理接口")
public class SysUserController extends BaseController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysPostService postService;
    private final SysDeptService deptService;

    /**
     * 获取用户列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    public TableDataInfo<SysUserVo> list(
            @Parameter(description = "用户查询参数")
            SysUserBo user,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }

    @Log(title = "Cross-tenant user query", businessType = BusinessType.CROSS_TENANT_QUERY)
    @GetMapping("/allList")
    @Operation(summary = "获取用户列表", description = "获取用户列表")
    public List<SysUserVo> allList(
            @Parameter(description = "申购单查询参数")
            SysUserBo bo) {
        return userService.selectUserList(bo);
    }

    @Log(title = "Cross-tenant dept tree query", businessType = BusinessType.CROSS_TENANT_QUERY)
    @GetMapping("/allDeptTree")
    @Operation(summary = "获取部门树列表", description = "获取部门树形结构列表")
    public R<List<Tree<Long>>> allDeptTree(
            @Parameter(description = "部门查询参数")
            SysDeptBo dept) {
        return R.ok(deptService.selectDeptTreeList(dept));
    }





    /**
     * 导出用户列表
     */
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:user:export")
    @PostMapping("/export")
    @Operation(summary = "导出用户列表", description = "导出用户列表到Excel")
    public void export(
            @Parameter(description = "用户查询参数")
            SysUserBo user,
            HttpServletResponse response) {
        List<SysUserExportVo> list = userService.selectUserExportList(user);
        ExcelUtil.exportExcel(list, "用户数据", SysUserExportVo.class, response);
    }

    /**
     * 导入数据
     *
     * @param file          导入文件
     * @param updateSupport 是否更新已存在数据
     */
    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @RepeatSubmit()
    @Operation(summary = "导入用户数据", description = "从Excel文件导入用户数据")
    public R<Void> importData(
            @Parameter(description = "导入文件", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "是否更新已存在数据")
            boolean updateSupport) throws Exception {
        if (ObjectUtil.isNull(file) || file.isEmpty()
            || !StringUtils.equalsAnyIgnoreCase(FileUtil.extName(file.getOriginalFilename()), "xls", "xlsx")) {
            return R.fail(MessageUtils.message("upload.invalidFileType", Map.of("types", "xls/xlsx")));
        }
        ExcelResult<SysUserImportVo> result = ExcelUtil.importExcel(file.getInputStream(), SysUserImportVo.class, new SysUserImportListener(updateSupport));
        return R.ok(result.getAnalysis());
    }

    /**
     * 获取导入模板
     */
    @PostMapping("/importTemplate")
    @Operation(summary = "获取导入模板", description = "获取用户导入Excel模板")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "用户数据", SysUserImportVo.class, response);
    }

    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    @Operation(summary = "根据用户编号获取详细信息", description = "根据用户ID获取用户详细信息")
    public R<Map<String, Object>> getInfo(
            @Parameter(description = "用户ID")
            @PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        Map<String, Object> ajax = new HashMap<>();
        SysRoleBo role = new SysRoleBo();
        role.setStatus(UserConstants.ROLE_NORMAL);
        SysPostBo post = new SysPostBo();
        post.setStatus(UserConstants.POST_NORMAL);
        List<SysRoleVo> roles = roleService.selectRoleList(role);
        ajax.put("roles", LoginHelper.isAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isAdmin()));
        ajax.put("posts", postService.selectPostList(post));
        if (ObjectUtil.isNotNull(userId)) {
            SysUserVo sysUser = userService.selectUserById(userId);
            ajax.put("user", sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", StreamUtils.toList(sysUser.getRoles(), SysRoleVo::getRoleId));
        }
        return R.ok(ajax);
    }

    /**
     * 新增用户
     */
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增用户", description = "新增用户信息")
    public R<Void> add(
            @Parameter(description = "用户信息", required = true)
            @Validated @RequestBody SysUserBo user) {
        deptService.checkDeptDataScope(user.getDeptId());
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(userService.resolveInitialPassword(user.getPassword()));
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    @GetMapping("/init-password")
    @Operation(summary = "获取管理员初始密码", description = "每次请求返回可用于新增用户的初始密码")
    public R<String> initPassword() {
        return R.ok(userService.resolveInitialPassword(StringUtils.EMPTY));
    }

    /**
     * 修改用户
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping
    @Operation(summary = "修改用户", description = "修改用户信息")
    public R<Void> edit(
            @Parameter(description = "用户信息", required = true)
            @Validated @RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     *
     * @param userIds 角色ID串
     */
    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    @Operation(summary = "删除用户", description = "批量删除用户")
    public R<Void> remove(
            @Parameter(description = "用户ID数组", required = true)
            @PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            return R.fail(MessageUtils.message("user.current.delete.denied"));
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/resetPwd")
    @Operation(summary = "重置密码", description = "重置用户密码")
    public R<Void> resetPwd(
            @Parameter(description = "用户信息", required = true)
            @RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        String hashpw = BCrypt.hashpw(user.getPassword());
        user.setPassword(hashpw);
        return toAjax(userService.resetUserPwd(user.getUserId(), user.getPassword()));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/changeStatus")
    @Operation(summary = "状态修改", description = "修改用户状态")
    public R<Void> changeStatus(
            @Parameter(description = "用户信息", required = true)
            @RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.updateUserStatus(user.getUserId(), user.getStatus()));
    }

    /**
     * 根据用户编号获取授权角色
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/authRole/{userId}")
    @Operation(summary = "根据用户编号获取授权角色", description = "根据用户ID获取已授权的角色列表")
    public R<Map<String, Object>> authRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        SysUserVo user = userService.selectUserById(userId);
        List<SysRoleVo> roles = roleService.selectRolesAuthByUserId(userId);
        return R.ok(Map.of(
            "user", user,
            "roles", LoginHelper.isAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isAdmin())
        ));
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户Id
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/authRole")
    @Operation(summary = "用户授权角色", description = "给用户授予角色权限")
    public R<Void> insertAuthRole(
            @Parameter(description = "用户ID", required = true)
            Long userId,
            @Parameter(description = "角色ID数组", required = true)
            Long[] roleIds) {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return R.ok();
    }

    /**
     * 获取部门树列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/deptTree")
    @Operation(summary = "获取部门树列表", description = "获取部门树形结构列表")
    public R<List<Tree<Long>>> deptTree(
            @Parameter(description = "部门查询参数")
            SysDeptBo dept) {
        return R.ok(deptService.selectDeptTreeList(dept));
    }



}
