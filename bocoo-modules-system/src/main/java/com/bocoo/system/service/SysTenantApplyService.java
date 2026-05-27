package com.bocoo.system.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.enums.TenantApplyStatus;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.core.enums.UserType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mail.utils.MailUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.MerchantProfileBo;
import com.bocoo.system.domain.bo.SysTenantApplyBo;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysRoleMenu;
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.entity.SysTenantApply;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.domain.vo.SysTenantApplyVo;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.mapper.SysUserMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class SysTenantApplyService {

    private static final String MERCHANT_ADMIN_ROLE_KEY = "merchant_admin";

    private final SysTenantApplyMapper tenantApplyMapper;
    private final SysTenantMapper tenantMapper;
    private final SysUserService userService;
    private final MerchantProfileService merchantProfileService;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Value("${mail.enabled:false}")
    private Boolean mailEnabled;

    public void submit(SysTenantApplyBo bo) {
        boolean exists = tenantApplyMapper.exists(new LambdaQueryWrapper<SysTenantApply>()
            .eq(SysTenantApply::getEmail, bo.getEmail())
            .in(SysTenantApply::getStatus, TenantApplyStatus.PENDING.getCode(), TenantApplyStatus.APPROVED.getCode()));
        if (exists) {
            throw ServiceException.ofMessageKey("tenant.apply.exists");
        }
        checkUserEmailAvailable(bo.getEmail());
        SysTenantApply apply = MapstructUtils.convert(bo, SysTenantApply.class);
        sanitizePublicSubmitApply(apply);
        apply.setStatus(TenantApplyStatus.PENDING.getCode());
        tenantApplyMapper.insert(apply);
    }

    public TableDataInfo<SysTenantApplyVo> selectPage(SysTenantApplyBo bo, PageQuery pageQuery) {
        checkPlatformTenant();
        LambdaQueryWrapper<SysTenantApply> wrapper = new LambdaQueryWrapper<SysTenantApply>()
            .eq(ObjectUtil.isNotNull(bo.getApplyId()), SysTenantApply::getApplyId, bo.getApplyId())
            .eq(ObjectUtil.isNotNull(bo.getStatus()), SysTenantApply::getStatus, bo.getStatus())
            .like(ObjectUtil.isNotNull(bo.getMerchantName()), SysTenantApply::getMerchantName, bo.getMerchantName())
            .like(ObjectUtil.isNotNull(bo.getCompanyName()), SysTenantApply::getCompanyName, bo.getCompanyName())
            .like(ObjectUtil.isNotNull(bo.getEmail()), SysTenantApply::getEmail, bo.getEmail())
            .orderByDesc(SysTenantApply::getCreateTime);
        Page<SysTenantApplyVo> page = tenantApplyMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    public SysTenantApplyVo selectById(Long applyId) {
        checkPlatformTenant();
        return tenantApplyMapper.selectVoById(applyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long applyId) {
        checkPlatformTenant();
        SysTenantApply apply = tenantApplyMapper.selectById(applyId);
        if (apply == null) {
            throw ServiceException.ofMessageKey("tenant.apply.notFound");
        }
        if (!TenantApplyStatus.PENDING.getCode().equals(apply.getStatus())) {
            throw ServiceException.ofMessageKey("tenant.apply.approve.pendingOnly");
        }
        checkUserEmailAvailable(apply.getEmail());

        SysTenant tenant = new SysTenant();
        tenant.setTenantName(apply.getMerchantName());
        tenant.setTenantType(TenantType.MERCHANT.getCode());
        tenant.setContactName(resolveContactName(apply));
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
            MerchantProfileBo profile = buildMerchantProfile(apply, tenant);
            merchantProfileService.insertProfile(profile);
            SysRole merchantRole = ensureMerchantAdminRole(tenant.getTenantId());
            ensureMerchantRoleMenus(merchantRole.getRoleId());
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(merchantRole.getRoleId());
            userRoleMapper.insert(userRole);
        });

        apply.setTenantId(tenant.getTenantId());
        apply.setStatus(TenantApplyStatus.APPROVED.getCode());
        apply.setAuditBy(LoginHelper.getUsername());
        apply.setAuditById(LoginHelper.getUserId());
        apply.setAuditTime(TimeUtils.utcNow());
        tenantApplyMapper.updateById(apply);
        sendInitialPasswordMail(apply, tempPassword);
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

    private void checkUserEmailAvailable(String email) {
        boolean exists = TenantContextHolder.callWithIgnore(() -> userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, email)));
        if (exists) {
            throw ServiceException.ofMessageKey("tenant.apply.email.used");
        }
    }

    private void sanitizePublicSubmitApply(SysTenantApply apply) {
        apply.setApplyId(null);
        apply.setTenantId(null);
        apply.setAuditBy(null);
        apply.setAuditById(null);
        apply.setAuditTime(null);
        apply.setRejectReason(null);
        apply.setCreateById(null);
        apply.setCreateBy(null);
        apply.setCreateTime(null);
        apply.setUpdateBy(null);
        apply.setUpdateTime(null);
    }

    private String resolveContactName(SysTenantApply apply) {
        if (ObjectUtil.isNotNull(apply.getContactName())) {
            return apply.getContactName();
        }
        String firstName = apply.getContactFirstName() == null ? "" : apply.getContactFirstName().trim();
        String lastName = apply.getContactLastName() == null ? "" : apply.getContactLastName().trim();
        return (firstName + " " + lastName).trim();
    }

    private MerchantProfileBo buildMerchantProfile(SysTenantApply apply, SysTenant tenant) {
        MerchantProfileBo profile = new MerchantProfileBo();
        profile.setTenantId(tenant.getTenantId());
        profile.setMerchantName(apply.getMerchantName());
        profile.setCompanyName(apply.getCompanyName());
        profile.setContactFirstName(apply.getContactFirstName());
        profile.setContactLastName(apply.getContactLastName());
        profile.setContactName(resolveContactName(apply));
        profile.setPrimaryEmail(apply.getEmail());
        profile.setOfficePhone(apply.getOfficePhone());
        profile.setMobilePhone(apply.getMobilePhone());
        profile.setCountry(apply.getCountry());
        profile.setState(apply.getState());
        profile.setCity(apply.getCity());
        profile.setAddressLine1(apply.getAddressLine1());
        profile.setAddressLine2(apply.getAddressLine2());
        profile.setPostalCode(apply.getPostalCode());
        profile.setStatus(UserStatus.OK.getCode());
        profile.setAuditStatus(TenantApplyStatus.APPROVED.getCode());
        profile.setRemark(apply.getRemark());
        return profile;
    }

    private SysRole ensureMerchantAdminRole(Long tenantId) {
        SysRole merchantRole = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleKey, MERCHANT_ADMIN_ROLE_KEY), false);
        if (merchantRole != null) {
            return merchantRole;
        }
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setRoleName("Merchant Admin");
        role.setRoleKey(MERCHANT_ADMIN_ROLE_KEY);
        role.setRoleSort(1);
        role.setDataScope("1");
        role.setMenuCheckStrictly(true);
        role.setDeptCheckStrictly(true);
        role.setStatus(UserStatus.OK.getCode());
        role.setDelFlag(UserConstants.NOT_DELETED);
        roleMapper.insert(role);
        return role;
    }

    private void ensureMerchantRoleMenus(Long roleId) {
        List<Long> menuIds = ensureMerchantMenus();
        if (menuIds.isEmpty()) {
            return;
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        List<SysRoleMenu> roleMenus = new ArrayList<>();
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        }
        roleMenuMapper.insertBatch(roleMenus);
    }

    private List<Long> ensureMerchantMenus() {
        SysMenu parent = selectOrCreateMenu(null, "Merchant", "menu.merchant", "merchant", UserConstants.TYPE_DIR, null, 10);
        SysMenu profile = selectOrCreateMenu(parent.getMenuId(), "Merchant Profile", "menu.merchant.profile",
            "profile", UserConstants.TYPE_MENU, "merchant:profile:query", 1);
        SysMenu query = selectOrCreateMenu(profile.getMenuId(), "Merchant Profile Query", "menu.merchant.profile.query",
            "", UserConstants.TYPE_BUTTON, "merchant:profile:query", 1);
        SysMenu edit = selectOrCreateMenu(profile.getMenuId(), "Merchant Profile Edit", "menu.merchant.profile.edit",
            "", UserConstants.TYPE_BUTTON, "merchant:profile:edit", 2);
        return List.of(parent.getMenuId(), profile.getMenuId(), query.getMenuId(), edit.getMenuId());
    }

    private SysMenu selectOrCreateMenu(Long parentId, String menuName, String i18nKey, String path,
                                      String menuType, String perms, Integer orderNum) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getMenuType, menuType)
            .eq(SysMenu::getMenuName, menuName)
            .eq(parentId != null, SysMenu::getParentId, parentId)
            .eq(ObjectUtil.isNotNull(perms), SysMenu::getPerms, perms);
        SysMenu menu = menuMapper.selectOne(wrapper, false);
        if (menu != null) {
            return menu;
        }
        menu = new SysMenu();
        menu.setMenuName(menuName);
        menu.setI18nKey(i18nKey);
        menu.setParentId(parentId == null ? 0L : parentId);
        menu.setOrderNum(orderNum);
        menu.setPath(path);
        if (UserConstants.TYPE_DIR.equals(menuType)) {
            menu.setComponent(UserConstants.LAYOUT);
        } else if (UserConstants.TYPE_MENU.equals(menuType)) {
            menu.setComponent("merchant/Profile");
        }
        menu.setIsFrame(UserConstants.NO_FRAME);
        menu.setIsCache("0");
        menu.setMenuType(menuType);
        menu.setVisible(UserConstants.TYPE_BUTTON.equals(menuType) ? "0" : "1");
        menu.setStatus(UserConstants.MENU_NORMAL);
        menu.setPerms(perms);
        menu.setIcon(UserConstants.TYPE_DIR.equals(menuType) ? "shop" : "form");
        menuMapper.insert(menu);
        return menu;
    }

    private void sendInitialPasswordMail(SysTenantApply apply, String tempPassword) {
        if (!Boolean.TRUE.equals(mailEnabled)) {
            log.warn("Merchant initial password email skipped because mail is disabled. applyId={}, email={}",
                apply.getApplyId(), apply.getEmail());
            return;
        }
        try {
            String subject = MessageUtils.message("tenant.apply.mail.subject");
            String content = MessageUtils.message("tenant.apply.mail.content", apply.getEmail(), tempPassword);
            MailUtils.sendText(apply.getEmail(), subject, content);
        } catch (Exception ex) {
            log.warn("Merchant initial password email failed. applyId={}, email={}, reason={}",
                apply.getApplyId(), apply.getEmail(), ex.getMessage());
        }
    }
}
