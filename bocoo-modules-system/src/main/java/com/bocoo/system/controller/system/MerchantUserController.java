package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.core.enums.UserType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysUserBo;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.service.MerchantAccountDefaultsService;
import com.bocoo.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/merchant/user")
@Tag(name = "Merchant users", description = "Merchant user management APIs")
public class MerchantUserController extends BaseController {

    private final SysUserService userService;
    private final MerchantAccountDefaultsService merchantDefaultsService;

    @SaCheckPermission("merchant:user:list")
    @GetMapping("/list")
    @Operation(summary = "List current merchant users")
    public TableDataInfo<SysUserVo> list(SysUserBo user, PageQuery pageQuery) {
        checkMerchantTenant();
        return userService.selectPageMerchantUserList(user, pageQuery);
    }

    @SaCheckPermission("merchant:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    @Operation(summary = "Get merchant user detail")
    public R<Map<String, Object>> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        checkMerchantTenant();
        Map<String, Object> ajax = new HashMap<>();
        ajax.put("roles", selectMerchantRoles());
        ajax.put("posts", List.of());
        if (ObjectUtil.isNotNull(userId)) {
            userService.checkUserDataScope(userId);
            SysUserVo sysUser = userService.selectUserById(userId);
            ajax.put("user", sysUser);
            ajax.put("postIds", List.of());
            ajax.put("roleIds", StreamUtils.toList(sysUser.getRoles(), SysRoleVo::getRoleId));
        } else {
            ajax.put("roleIds", List.of(merchantDefaultsService.ensureStoreRole().getRoleId()));
        }
        return R.ok(ajax);
    }

    @SaCheckPermission("merchant:user:add")
    @Log(title = "Merchant users", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "Create merchant store user")
    public R<Void> add(@Validated @RequestBody SysUserBo user) {
        checkMerchantTenant();
        SysDept storeDept = merchantDefaultsService.ensureStoreDept();
        SysRole storeRole = merchantDefaultsService.ensureStoreRole();
        user.setTenantId(LoginHelper.getTenantId());
        user.setDeptId(storeDept.getDeptId());
        user.setRoleIds(new Long[] {storeRole.getRoleId()});
        user.setPostIds(new Long[0]);
        user.setUserType(UserType.SYS_USER.getUserType());
        if (StringUtils.isBlank(user.getStatus())) {
            user.setStatus(UserStatus.OK.getCode());
        }
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    @SaCheckPermission("merchant:user:edit")
    @Log(title = "Merchant users", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping
    @Operation(summary = "Update merchant user basic fields")
    public R<Void> edit(@Validated @RequestBody SysUserBo user) {
        checkMerchantTenant();
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return toAjax(userService.updateMerchantUser(user));
    }

    @SaCheckPermission("merchant:user:remove")
    @Log(title = "Merchant users", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    @Operation(summary = "Delete merchant users")
    public R<Void> remove(@PathVariable Long[] userIds) {
        checkMerchantTenant();
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            return R.fail(MessageUtils.message("user.current.delete.denied"));
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    @SaCheckPermission("merchant:user:resetPwd")
    @Log(title = "Merchant users", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/resetPwd")
    @Operation(summary = "Reset merchant user password")
    public R<Void> resetPwd(@RequestBody SysUserBo user) {
        checkMerchantTenant();
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(user.getPassword())));
    }

    @SaCheckPermission("merchant:user:edit")
    @Log(title = "Merchant users", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/changeStatus")
    @Operation(summary = "Change merchant user status")
    public R<Void> changeStatus(@RequestBody SysUserBo user) {
        checkMerchantTenant();
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.updateUserStatus(user.getUserId(), user.getStatus()));
    }

    private List<SysRoleVo> selectMerchantRoles() {
        MerchantAccountDefaultsService.MerchantDefaults defaults = merchantDefaultsService.ensureDefaults();
        return List.of(toRoleVo(defaults.dealerRole()), toRoleVo(defaults.storeRole()));
    }

    private SysRoleVo toRoleVo(SysRole role) {
        SysRoleVo vo = new SysRoleVo();
        vo.setRoleId(role.getRoleId());
        vo.setTenantId(role.getTenantId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleSort(role.getRoleSort());
        vo.setStatus(role.getStatus());
        return vo;
    }

    private void checkMerchantTenant() {
        if (!TenantType.MERCHANT.getCode().equals(LoginHelper.getTenantType())) {
            throw ServiceException.ofMessageKey("merchant.profile.merchantOnly");
        }
    }
}
