package com.bocoo.system.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.lock.annotation.Lock4j;
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
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.entity.SysTenantApply;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.entity.SysUserRole;
import com.bocoo.system.domain.vo.SysTenantApplyVo;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.mapper.SysUserMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
@Service
public class SysTenantApplyService {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final Duration EMAIL_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration EMAIL_CODE_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration EMAIL_CODE_ERROR_LOCK = Duration.ofMinutes(10);
    private static final Duration EMAIL_CODE_DAILY_WINDOW = Duration.ofHours(26);
    private static final int EMAIL_CODE_DAILY_LIMIT = 5;
    private static final int EMAIL_CODE_ERROR_LIMIT = 5;
    private static final String EMAIL_CODE_KEY_PREFIX = "merchant_apply:email_code:";
    private static final String EMAIL_CODE_COOLDOWN_PREFIX = "merchant_apply:email_code_cooldown:";
    private static final String EMAIL_CODE_DAILY_PREFIX = "merchant_apply:email_code_daily:";
    private static final String EMAIL_CODE_ERROR_PREFIX = "merchant_apply:email_code_error:";

    private final SysTenantApplyMapper tenantApplyMapper;
    private final SysTenantMapper tenantMapper;
    private final SysUserService userService;
    private final MerchantProfileService merchantProfileService;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final MerchantAccountDefaultsService merchantDefaultsService;

    @Value("${mail.enabled:false}")
    private Boolean mailEnabled;

    public void sendEmailCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        String cooldownKey = EMAIL_CODE_COOLDOWN_PREFIX + normalizedEmail;
        if (!RedisUtils.setObjectIfAbsent(cooldownKey, "1", EMAIL_CODE_COOLDOWN)) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.cooldown");
        }
        if (!isEmailAvailableForPublicApply(normalizedEmail)) {
            log.info("Merchant application email verification code request accepted without sending. email={}", normalizedEmail);
            return;
        }
        enforceDailyEmailCodeLimit(normalizedEmail);
        if (!Boolean.TRUE.equals(mailEnabled)) {
            RedisUtils.deleteObject(cooldownKey);
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.mailDisabled");
        }
        String code = RandomUtil.randomNumbers(6);
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
        checkPublicApplyEmailAvailable(normalizedEmail);
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

    @Lock4j(keys = {"#applyId"})
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
            MerchantAccountDefaultsService.MerchantDefaults defaults = merchantDefaultsService.ensureDefaults();
            com.bocoo.system.domain.bo.SysUserBo user = new com.bocoo.system.domain.bo.SysUserBo();
            user.setTenantId(tenant.getTenantId());
            user.setDeptId(defaults.dealerDept().getDeptId());
            user.setUserName(finalApply.getEmail());
            user.setEmail(finalApply.getEmail());
            user.setNickName(finalApply.getMerchantName());
            user.setPassword(BCrypt.hashpw(tempPassword));
            user.setForcePasswordChange(UserConstants.FORCE_PASSWORD_CHANGE_YES);
            user.setUserType(UserType.SYS_USER.getUserType());
            user.setStatus(UserStatus.OK.getCode());
            userService.registerUser(user);
            MerchantProfileBo profile = buildMerchantProfile(apply, tenant);
            merchantProfileService.insertProfile(profile);
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(defaults.dealerRole().getRoleId());
            userRoleMapper.insert(userRole);
        });

        apply.setTenantId(tenant.getTenantId());
        apply.setStatus(TenantApplyStatus.APPROVED.getCode());
        apply.setAuditBy(LoginHelper.getUsername());
        apply.setAuditById(LoginHelper.getUserId());
        apply.setAuditTime(TimeUtils.utcNow());
        tenantApplyMapper.updateById(apply);
        runAfterCommit(() -> sendInitialPasswordMail(apply, tempPassword));
    }

    @Lock4j(keys = {"#applyId"})
    @Transactional(rollbackFor = Exception.class)
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
        runAfterCommit(() -> sendRejectedMail(apply));
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
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

    private boolean isEmailAvailableForPublicApply(String email) {
        boolean userExists = TenantContextHolder.callWithIgnore(() -> userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, email)));
        if (userExists) {
            return false;
        }
        return !tenantApplyMapper.exists(new LambdaQueryWrapper<SysTenantApply>()
            .eq(SysTenantApply::getEmail, email)
            .in(SysTenantApply::getStatus, TenantApplyStatus.PENDING.getCode(), TenantApplyStatus.APPROVED.getCode()));
    }

    private void checkPublicApplyEmailAvailable(String email) {
        if (!isEmailAvailableForPublicApply(email)) {
            throw ServiceException.ofMessageKey("tenant.apply.submit.unavailable");
        }
    }

    private void enforceDailyEmailCodeLimit(String email) {
        String day = TimeUtils.formatUtc(TimeUtils.utcNow(), TimeUtils.UTC_DATE_FORMATTER);
        String key = EMAIL_CODE_DAILY_PREFIX + day + ":" + email;
        long count = RedisUtils.incrAtomicValue(key);
        if (count == 1) {
            RedisUtils.expire(key, EMAIL_CODE_DAILY_WINDOW);
        }
        if (count > EMAIL_CODE_DAILY_LIMIT) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.dailyLimit");
        }
    }

    private void checkEmailCode(String email, String inputCode) {
        if (StrUtil.isBlank(inputCode)) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.required");
        }
        String key = EMAIL_CODE_KEY_PREFIX + email;
        String cachedCode = RedisUtils.getCacheObject(key);
        if (!inputCode.equals(cachedCode)) {
            recordEmailCodeFailure(email);
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.invalid");
        }
        RedisUtils.deleteObject(EMAIL_CODE_ERROR_PREFIX + email);
        RedisUtils.deleteObject(key);
    }

    private void recordEmailCodeFailure(String email) {
        String key = EMAIL_CODE_ERROR_PREFIX + email;
        long count = RedisUtils.incrAtomicValue(key);
        if (count == 1) {
            RedisUtils.expire(key, EMAIL_CODE_ERROR_LOCK);
        }
        if (count >= EMAIL_CODE_ERROR_LIMIT) {
            throw ServiceException.ofMessageKey("tenant.apply.emailCode.retryLimit");
        }
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
