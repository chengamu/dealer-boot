package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.core.enums.UserType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MessageUtils;
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
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.service.MerchantAccountDefaultsService;
import com.bocoo.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/merchant/user")
@Tag(name = "Merchant users", description = "Merchant user management APIs")
public class MerchantUserController extends BaseController {

    private final SysUserService userService;
    private final MerchantAccountDefaultsService merchantDefaultsService;
    private final SysTenantMapper tenantMapper;

    @SaCheckPermission("merchant:user:list")
    @GetMapping("/list")
    @Operation(summary = "List current merchant users")
    public TableDataInfo<SysUserVo> list(SysUserBo user, PageQuery pageQuery) {
        if (LoginHelper.isPlatformTenant()) {
            if (ObjectUtil.isNotNull(user.getTenantId())) {
                validateMerchantTenant(user.getTenantId());
            }
            boolean merchantTenantOnly = ObjectUtil.isNull(user.getTenantId());
            return userService.selectPageMerchantUserList(user, pageQuery, merchantTenantOnly);
        }
        Long targetTenantId = resolveCurrentMerchantTenantId();
        user.setTenantId(targetTenantId);
        return withTargetTenant(targetTenantId, () -> userService.selectPageMerchantUserList(user, pageQuery));
    }

    @SaCheckPermission("merchant:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    @Operation(summary = "Get merchant user detail")
    public R<Map<String, Object>> getInfo(@PathVariable(value = "userId", required = false) Long userId,
                                          @RequestParam(value = "tenantId", required = false) Long tenantId) {
        Long targetTenantId = resolveTargetTenantId(tenantId);
        Map<String, Object> ajax = new HashMap<>();
        if (ObjectUtil.isNotNull(userId)) {
            SysUserVo sysUser = withTargetTenant(targetTenantId, () -> {
                userService.checkUserDataScope(userId);
                return userService.selectUserById(userId);
            });
            sysUser.setDeptId(null);
            sysUser.setDeptName(null);
            sysUser.setRoles(null);
            sysUser.setRoleIds(null);
            sysUser.setRoleId(null);
            sysUser.setPostIds(null);
            ajax.put("user", sysUser);
        }
        return R.ok(ajax);
    }

    @SaCheckPermission("merchant:user:add")
    @Log(title = "Merchant users", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "Create merchant store user")
    public R<Void> add(@Validated @RequestBody SysUserBo user) {
        Long targetTenantId = resolveTargetTenantId(user.getTenantId());
        return withTargetTenant(targetTenantId, () -> addInTenant(user, targetTenantId));
    }

    private R<Void> addInTenant(SysUserBo user, Long targetTenantId) {
        SysDept defaultDept = merchantDefaultsService.ensureDefaultDept();
        SysRole employeeRole = merchantDefaultsService.ensureEmployeeRole();
        user.setTenantId(targetTenantId);
        user.setDeptId(defaultDept.getDeptId());
        user.setRoleIds(new Long[] {employeeRole.getRoleId()});
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
        user.setPassword(userService.resolveInitialPassword(user.getPassword()));
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    @SaCheckPermission("merchant:user:edit")
    @Log(title = "Merchant users", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping
    @Operation(summary = "Update merchant user basic fields")
    public R<Void> edit(@Validated @RequestBody SysUserBo user) {
        Long targetTenantId = resolveTargetTenantId(user.getTenantId());
        return withTargetTenant(targetTenantId, () -> editInTenant(user));
    }

    private R<Void> editInTenant(SysUserBo user) {
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
    public R<Void> remove(@PathVariable Long[] userIds,
                          @RequestParam(value = "tenantId", required = false) Long tenantId) {
        Long targetTenantId = resolveTargetTenantId(tenantId);
        return withTargetTenant(targetTenantId, () -> removeInTenant(userIds));
    }

    private R<Void> removeInTenant(Long[] userIds) {
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            return R.fail(MessageUtils.message("user.current.delete.denied"));
        }
        for (Long userId : userIds) {
            if (userService.userHasRoleKey(userId, UserConstants.ADMIN_ROLE_KEY)
                || userService.userHasRoleKey(userId, MerchantAccountDefaultsService.OWNER_ROLE_KEY)) {
                return R.fail(MessageUtils.message("merchant.user.admin.delete.denied"));
            }
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    @SaCheckPermission("merchant:user:resetPwd")
    @Log(title = "Merchant users", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/resetPwd")
    @Operation(summary = "Reset merchant user password")
    public R<Void> resetPwd(@RequestBody SysUserBo user) {
        Long targetTenantId = resolveTargetTenantId(user.getTenantId());
        return withTargetTenant(targetTenantId, () -> {
            userService.checkUserAllowed(user.getUserId());
            userService.checkUserDataScope(user.getUserId());
            return toAjax(userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(user.getPassword())));
        });
    }

    @SaCheckPermission("merchant:user:edit")
    @Log(title = "Merchant users", businessType = BusinessType.SENSITIVE_OPERATION)
    @PutMapping("/changeStatus")
    @Operation(summary = "Change merchant user status")
    public R<Void> changeStatus(@RequestBody SysUserBo user) {
        Long targetTenantId = resolveTargetTenantId(user.getTenantId());
        return withTargetTenant(targetTenantId, () -> {
            userService.checkUserAllowed(user.getUserId());
            userService.checkUserDataScope(user.getUserId());
            return toAjax(userService.updateUserStatus(user.getUserId(), user.getStatus()));
        });
    }

    private Long resolveTargetTenantId(Long requestTenantId) {
        if (LoginHelper.isPlatformTenant()) {
            if (ObjectUtil.isNull(requestTenantId)) {
                throw ServiceException.ofMessageKey("merchant.user.tenant.required");
            }
            validateMerchantTenant(requestTenantId);
            return requestTenantId;
        }
        return resolveCurrentMerchantTenantId();
    }

    private void validateMerchantTenant(Long tenantId) {
        SysTenant tenant = TenantContextHolder.callWithIgnore(() -> tenantMapper.selectById(tenantId));
        if (ObjectUtil.isNull(tenant) || !TenantType.MERCHANT.getCode().equals(tenant.getTenantType())) {
            throw ServiceException.ofMessageKey("merchant.profile.merchantOnly");
        }
    }

    private Long resolveCurrentMerchantTenantId() {
        Long tenantId = LoginHelper.getTenantId();
        if (ObjectUtil.isNull(tenantId)) {
            throw ServiceException.ofMessageKey("merchant.user.tenant.required");
        }
        return tenantId;
    }

    private <T> T withTargetTenant(Long tenantId, Supplier<T> supplier) {
        if (ObjectUtil.isNull(tenantId)) {
            throw ServiceException.ofMessageKey("merchant.user.tenant.required");
        }
        if (LoginHelper.isPlatformTenant()) {
            return TenantContextHolder.callWithTenant(tenantId, supplier);
        }
        if (!TenantType.MERCHANT.getCode().equals(LoginHelper.getTenantType())) {
            throw ServiceException.ofMessageKey("merchant.profile.merchantOnly");
        }
        return supplier.get();
    }
}
