package com.bocoo.system.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
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
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.MerchantProfileBo;
import com.bocoo.system.domain.bo.SysTenantApplyBo;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysRoleMenu;
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.entity.SysTenantApply;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysDeptMapper;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
@Service
public class SysTenantApplyService {

    private static final String MERCHANT_ADMIN_ROLE_KEY = "merchant_admin";
    private static final String MERCHANT_DEPT_NAME = "商家";
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final Duration EMAIL_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration EMAIL_CODE_COOLDOWN = Duration.ofSeconds(60);
    private static final String EMAIL_CODE_KEY_PREFIX = "merchant_apply:email_code:";
    private static final String EMAIL_CODE_COOLDOWN_PREFIX = "merchant_apply:email_code_cooldown:";

    private final SysTenantApplyMapper tenantApplyMapper;
    private final SysTenantMapper tenantMapper;
    private final SysUserService userService;
    private final MerchantProfileService merchantProfileService;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Value("${mail.enabled:false}")
    private Boolean mailEnabled;

    public void sendEmailCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        checkApplyEmailAvailable(normalizedEmail);
        checkUserEmailAvailable(normalizedEmail);
        String cooldownKey = EMAIL_CODE_COOLDOWN_PREFIX + normalizedEmail;
        if (!RedisUtils.setObjectIfAbsent(cooldownKey, "1", EMAIL_CODE_COOLDOWN)) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.cooldown");
        }
        if (!Boolean.TRUE.equals(mailEnabled)) {
            RedisUtils.deleteObject(cooldownKey);
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.mailDisabled");
        }
        String code = RandomUtil.randomNumbers(4);
        try {
            String subject = MessageUtils.message("tenant.apply.emailCode.subject");
            String content = buildEmailCodeHtml(code);
            MailUtils.sendHtml(normalizedEmail, subject, content);
            RedisUtils.setCacheObject(EMAIL_CODE_KEY_PREFIX + normalizedEmail, code, EMAIL_CODE_TTL);
        } catch (Exception ex) {
            RedisUtils.deleteObject(cooldownKey);
            log.warn("Merchant application email verification code failed. email={}, reason={}",
                normalizedEmail, ex.getMessage());
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.sendFailed");
        }
    }

    public void submit(SysTenantApplyBo bo) {
        if (!Boolean.TRUE.equals(bo.getTermsAccepted())) {
            throw ServiceException.ofMessageKey("tenant.apply.terms.required");
        }
        String normalizedEmail = normalizeEmail(bo.getEmail());
        bo.setEmail(normalizedEmail);
        checkEmailCode(normalizedEmail, bo.getVerificationCode());
        checkApplyEmailAvailable(normalizedEmail);
        checkUserEmailAvailable(normalizedEmail);
        SysTenantApply apply = MapstructUtils.convert(bo, SysTenantApply.class);
        apply.setApplyLocale(resolveCurrentLocale());
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

        String tempPassword = RandomUtil.randomString(PASSWORD_CHARS, 12);
        SysTenantApply finalApply = apply;
        TenantContextHolder.runWithTenant(tenant.getTenantId(), () -> {
            Long deptId = ensureMerchantDept(finalApply);
            com.bocoo.system.domain.bo.SysUserBo user = new com.bocoo.system.domain.bo.SysUserBo();
            user.setTenantId(tenant.getTenantId());
            user.setDeptId(deptId);
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
        sendRejectedMail(apply);
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

    private void checkApplyEmailAvailable(String email) {
        boolean exists = tenantApplyMapper.exists(new LambdaQueryWrapper<SysTenantApply>()
            .eq(SysTenantApply::getEmail, email)
            .in(SysTenantApply::getStatus, TenantApplyStatus.PENDING.getCode(), TenantApplyStatus.APPROVED.getCode()));
        if (exists) {
            throw ServiceException.ofMessageKey("tenant.apply.exists");
        }
    }

    private void checkEmailCode(String email, String inputCode) {
        if (StrUtil.isBlank(inputCode)) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.required");
        }
        String key = EMAIL_CODE_KEY_PREFIX + email;
        String cachedCode = RedisUtils.getCacheObject(key);
        if (!inputCode.equals(cachedCode)) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.invalid");
        }
        RedisUtils.deleteObject(key);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String resolveCurrentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "zh".equalsIgnoreCase(locale.getLanguage())) {
            return "zh_CN";
        }
        return "en_US";
    }

    private Locale resolveApplyLocale(SysTenantApply apply) {
        String applyLocale = apply.getApplyLocale();
        if ("zh_CN".equalsIgnoreCase(applyLocale)) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return Locale.US;
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

    private Long ensureMerchantDept(SysTenantApply apply) {
        SysDept dept = deptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getDeptName, MERCHANT_DEPT_NAME), false);
        if (dept != null) {
            return dept.getDeptId();
        }
        dept = new SysDept();
        dept.setParentId(0L);
        dept.setAncestors("0");
        dept.setDeptName(MERCHANT_DEPT_NAME);
        dept.setOrderNum(1);
        dept.setLeader(resolveContactName(apply));
        dept.setPhone(normalizeDeptPhone(StrUtil.blankToDefault(apply.getOfficePhone(), apply.getMobilePhone())));
        dept.setEmail(apply.getEmail());
        dept.setStatus(UserConstants.DEPT_NORMAL);
        dept.setDelFlag(UserConstants.NOT_DELETED);
        deptMapper.insert(dept);
        return dept.getDeptId();
    }

    private String normalizeDeptPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (StrUtil.isBlank(digits)) {
            return null;
        }
        return StrUtil.subPre(digits, 11);
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
            Locale locale = resolveApplyLocale(apply);
            String subject = MessageUtils.message(locale, "tenant.apply.mail.subject");
            String content = buildInitialPasswordHtml(apply, tempPassword, locale);
            MailUtils.sendHtml(apply.getEmail(), subject, content);
        } catch (Exception ex) {
            log.warn("Merchant initial password email failed. applyId={}, email={}, reason={}",
                apply.getApplyId(), apply.getEmail(), ex.getMessage());
        }
    }

    private void sendRejectedMail(SysTenantApply apply) {
        if (!Boolean.TRUE.equals(mailEnabled)) {
            log.warn("Merchant rejection email skipped because mail is disabled. applyId={}, email={}",
                apply.getApplyId(), apply.getEmail());
            return;
        }
        try {
            Locale locale = resolveApplyLocale(apply);
            String subject = MessageUtils.message(locale, "tenant.apply.reject.mail.subject");
            String content = buildRejectedHtml(apply, locale);
            MailUtils.sendHtml(apply.getEmail(), subject, content);
        } catch (Exception ex) {
            log.warn("Merchant rejection email failed. applyId={}, email={}, reason={}",
                apply.getApplyId(), apply.getEmail(), ex.getMessage());
        }
    }

    private String buildEmailCodeHtml(String code) {
        return """
            <div style="font-family:Arial,sans-serif;background:#f4f7fb;padding:28px;">
              <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;padding:28px;border:1px solid #dbe5f2;">
                <h2 style="margin:0 0 12px;color:#06184a;">skyspf</h2>
                <p style="margin:0 0 16px;color:#334155;">%s</p>
                <div style="font-size:32px;letter-spacing:8px;font-weight:700;color:#075cff;background:#eef6ff;padding:16px 20px;border-radius:10px;text-align:center;">%s</div>
                <p style="margin:18px 0 0;color:#64748b;font-size:13px;">%s</p>
              </div>
            </div>
            """.formatted(
            MessageUtils.message("tenant.apply.emailCode.body"),
            code,
            MessageUtils.message("tenant.apply.emailCode.expireHint"));
    }

    private String buildInitialPasswordHtml(SysTenantApply apply, String tempPassword, Locale locale) {
        return buildMailShell(
            MessageUtils.message(locale, "tenant.apply.mail.heading"),
            MessageUtils.message(locale, "tenant.apply.mail.body", apply.getMerchantName()),
            MessageUtils.message(locale, "tenant.apply.mail.accountLabel") + ": " + apply.getEmail(),
            MessageUtils.message(locale, "tenant.apply.mail.passwordLabel") + ": " + tempPassword,
            MessageUtils.message(locale, "tenant.apply.mail.footer"));
    }

    private String buildRejectedHtml(SysTenantApply apply, Locale locale) {
        String reason = StrUtil.blankToDefault(apply.getRejectReason(), MessageUtils.message(locale, "tenant.apply.reject.defaultReason"));
        return buildMailShell(
            MessageUtils.message(locale, "tenant.apply.reject.mail.heading"),
            MessageUtils.message(locale, "tenant.apply.reject.mail.body", apply.getMerchantName()),
            MessageUtils.message(locale, "tenant.apply.reject.mail.reasonLabel"),
            reason,
            MessageUtils.message(locale, "tenant.apply.reject.mail.footer"));
    }

    private String buildMailShell(String heading, String body, String label, String value, String footer) {
        return """
            <div style="font-family:Arial,sans-serif;background:#f4f7fb;padding:28px;">
              <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:12px;padding:30px;border:1px solid #dbe5f2;">
                <div style="font-size:24px;font-weight:700;color:#06184a;margin-bottom:18px;">skyspf</div>
                <h2 style="margin:0 0 12px;color:#06184a;">%s</h2>
                <p style="margin:0 0 20px;color:#334155;line-height:1.6;">%s</p>
                <div style="background:#eef6ff;border-radius:10px;padding:16px 18px;color:#001b5d;line-height:1.8;">
                  <strong>%s</strong><br/>
                  <span>%s</span>
                </div>
                <p style="margin:18px 0 0;color:#64748b;font-size:13px;line-height:1.6;">%s</p>
              </div>
            </div>
            """.formatted(heading, body, label, value, footer);
    }
}
