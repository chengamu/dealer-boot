package com.bocoo.system.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantApplyStatus;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.core.enums.UserType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.SysTenantApplyBo;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.entity.SysTenantApply;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.SysTenantApplyVo;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SysTenantApplyService {

    private static final String MERCHANT_ADMIN_ROLE_KEY = "merchant_admin";

    private final SysTenantApplyMapper tenantApplyMapper;
    private final SysTenantMapper tenantMapper;
    private final SysUserService userService;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public void submit(SysTenantApplyBo bo) {
        boolean exists = tenantApplyMapper.exists(new LambdaQueryWrapper<SysTenantApply>()
            .eq(SysTenantApply::getEmail, bo.getEmail())
            .in(SysTenantApply::getStatus, TenantApplyStatus.PENDING.getCode(), TenantApplyStatus.APPROVED.getCode()));
        if (exists) {
            throw ServiceException.ofMessageKey("tenant.apply.exists");
        }
        SysTenantApply apply = MapstructUtils.convert(bo, SysTenantApply.class);
        apply.setStatus(TenantApplyStatus.PENDING.getCode());
        tenantApplyMapper.insert(apply);
    }

    public TableDataInfo<SysTenantApplyVo> selectPage(SysTenantApplyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysTenantApply> wrapper = new LambdaQueryWrapper<SysTenantApply>()
            .eq(ObjectUtil.isNotNull(bo.getApplyId()), SysTenantApply::getApplyId, bo.getApplyId())
            .eq(ObjectUtil.isNotNull(bo.getStatus()), SysTenantApply::getStatus, bo.getStatus())
            .like(ObjectUtil.isNotNull(bo.getMerchantName()), SysTenantApply::getMerchantName, bo.getMerchantName())
            .like(ObjectUtil.isNotNull(bo.getEmail()), SysTenantApply::getEmail, bo.getEmail())
            .orderByDesc(SysTenantApply::getCreateTime);
        Page<SysTenantApplyVo> page = tenantApplyMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    public SysTenantApplyVo selectById(Long applyId) {
        return tenantApplyMapper.selectVoById(applyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public String approve(Long applyId) {
        checkPlatformTenant();
        SysTenantApply apply = tenantApplyMapper.selectById(applyId);
        if (apply == null) {
            throw ServiceException.ofMessageKey("tenant.apply.notFound");
        }
        if (!TenantApplyStatus.PENDING.getCode().equals(apply.getStatus())) {
            throw ServiceException.ofMessageKey("tenant.apply.approve.pendingOnly");
        }

        SysTenant tenant = new SysTenant();
        tenant.setTenantName(apply.getMerchantName());
        tenant.setTenantType(TenantType.MERCHANT.getCode());
        tenant.setContactName(apply.getContactName());
        tenant.setContactEmail(apply.getEmail());
        tenant.setCountry(apply.getCountry());
        tenant.setStatus(UserStatus.OK.getCode());
        tenantMapper.insert(tenant);

        String tempPassword = "Tmp@" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        SysTenantApply finalApply = apply;
        TenantContextHolder.runWithTenant(tenant.getTenantId(), () -> {
            com.bocoo.system.domain.bo.SysUserBo user = new com.bocoo.system.domain.bo.SysUserBo();
            user.setTenantId(tenant.getTenantId());
            user.setUserName(finalApply.getEmail());
            user.setEmail(finalApply.getEmail());
            user.setNickName(finalApply.getMerchantName());
            user.setPassword(BCrypt.hashpw(tempPassword));
            user.setUserType(UserType.SYS_USER.getUserType());
            user.setStatus(UserStatus.OK.getCode());
            userService.registerUser(user);
            SysRole merchantRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, MERCHANT_ADMIN_ROLE_KEY), false);
            if (merchantRole != null) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(merchantRole.getRoleId());
                userRoleMapper.insert(userRole);
            } else {
                SysRole role = new SysRole();
                role.setTenantId(tenant.getTenantId());
                role.setRoleName("Merchant Admin");
                role.setRoleKey(MERCHANT_ADMIN_ROLE_KEY);
                role.setRoleSort(1);
                role.setDataScope("1");
                role.setMenuCheckStrictly(true);
                role.setDeptCheckStrictly(true);
                role.setStatus(UserStatus.OK.getCode());
                role.setDelFlag("0");
                roleMapper.insert(role);
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getUserId());
                userRole.setRoleId(role.getRoleId());
                userRoleMapper.insert(userRole);
            }
        });

        apply.setTenantId(tenant.getTenantId());
        apply.setStatus(TenantApplyStatus.APPROVED.getCode());
        apply.setAuditBy(LoginHelper.getUsername());
        apply.setAuditById(LoginHelper.getUserId());
        apply.setAuditTime(TimeUtils.utcNow());
        tenantApplyMapper.updateById(apply);
        return tempPassword;
    }

    public void reject(Long applyId, SysTenantApplyBo bo) {
        checkPlatformTenant();
        SysTenantApply apply = tenantApplyMapper.selectById(applyId);
        if (apply == null) {
            throw ServiceException.ofMessageKey("tenant.apply.notFound");
        }
        if (!TenantApplyStatus.PENDING.getCode().equals(apply.getStatus())) {
            throw ServiceException.ofMessageKey("tenant.apply.reject.pendingOnly");
        }
        apply.setStatus(TenantApplyStatus.REJECTED.getCode());
        apply.setRejectReason(bo.getRejectReason());
        apply.setAuditBy(LoginHelper.getUsername());
        apply.setAuditById(LoginHelper.getUserId());
        apply.setAuditTime(TimeUtils.utcNow());
        tenantApplyMapper.updateById(apply);
    }

    private void checkPlatformTenant() {
        if (!LoginHelper.isPlatformTenant()) {
            throw ServiceException.ofMessageKey("tenant.apply.audit.platformOnly");
        }
    }
}
