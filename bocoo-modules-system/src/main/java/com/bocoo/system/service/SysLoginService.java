package com.bocoo.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.constant.CacheConstants;
import com.bocoo.common.core.constant.Constants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.domain.bo.XcxLoginUser;
import com.bocoo.common.core.domain.vo.RoleVO;
import com.bocoo.common.core.enums.DeviceType;
import com.bocoo.common.core.enums.LoginType;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.exception.user.CaptchaException;
import com.bocoo.common.core.exception.user.CaptchaExpireException;
import com.bocoo.common.core.exception.user.UserException;
import com.bocoo.common.core.utils.*;
import com.bocoo.common.log.event.LogininforEvent;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.common.web.config.properties.CaptchaProperties;
import com.bocoo.system.domain.entity.SysTenant;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import com.bocoo.system.domain.vo.SysDeptVo;
import com.bocoo.system.domain.vo.SysRoleVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * 登录校验方法
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginService {

    private final SysUserMapper userMapper;
    private final CaptchaProperties captchaProperties;
    private final SysPermissionService permissionService;
    private final SysRoleService roleService;
    private final SysDeptService deptService;
    private final SysTenantMapper tenantMapper;
    private final MerchantProfileService merchantProfileService;

    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;

    @Value("${user.password.lockTime}")
    private Integer lockTime;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        boolean captchaEnabled = captchaProperties.getEnable();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(username, code, uuid);
        }
        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUserVo user = loadUserByUsernameOrEmail(username);
        checkLogin(LoginType.PASSWORD, username, () -> !BCrypt.checkpw(password, user.getPassword()));
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.PC);

        recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginInfo(user.getUserId(), username);
        return StpUtil.getTokenValue();
    }

    /**
     * 第三方登录方法
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功后生成的token值
     */
    public String thirdLogin(String username, String password) {

        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUserVo user = loadUserByUsernameOrEmail(username);

        // 验证用户登录信息
        checkLogin(LoginType.PASSWORD, username, () -> !BCrypt.checkpw(password, user.getPassword()));

        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);

        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.PC);

        // 记录登录成功信息
        recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginInfo(user.getUserId(), username);

        return StpUtil.getTokenValue();
    }


    public String smsLogin(String phonenumber, String smsCode) {
        // 通过手机号查找用户
        SysUserVo user = loadUserByPhonenumber(phonenumber);

        checkLogin(LoginType.SMS, user.getUserName(), () -> !validateSmsCode(phonenumber, smsCode));
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.APP);

        recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginInfo(user.getUserId(), user.getUserName());
        return StpUtil.getTokenValue();
    }

    public String emailLogin(String email, String emailCode) {
        // 通过手邮箱查找用户
        SysUserVo user = loadUserByEmail(email);

        checkLogin(LoginType.EMAIL, user.getUserName(), () -> !validateEmailCode(email, emailCode));
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.APP);

        recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginInfo(user.getUserId(), user.getUserName());
        return StpUtil.getTokenValue();
    }

    public String xcxLogin(String xcxCode) {
        // xcxCode 为 小程序调用 wx.login 授权后获取
        // todo 以下自行实现
        // 校验 appid + appsrcret + xcxCode 调用登录凭证校验接口 获取 session_key 与 openid
        String openid = "";

        // 框架登录不限制从什么表查询 只要最终构建出 LoginUser 即可
        SysUserVo user = loadUserByOpenid(openid);

        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        XcxLoginUser loginUser = new XcxLoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
        loginUser.setOpenid(openid);
        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.XCX);

        recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        recordLoginInfo(user.getUserId(), user.getUserName());
        return StpUtil.getTokenValue();
    }

    /**
     * 退出登录
     */
    public void logout() {
        try {
            LoginUser loginUser = LoginHelper.getLoginUser();
            recordLogininfor(loginUser.getUsername(), Constants.LOGOUT, MessageUtils.message("user.logout.success"));
        } catch (NotLoginException ignored) {
        } finally {
            try {
                StpUtil.logout();
            } catch (NotLoginException ignored) {
            }
        }
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     */
    private void recordLogininfor(String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        var request = ServletUtils.getRequest();
        logininforEvent.setRequest(request);
        logininforEvent.setIpaddr(ServletUtils.getClientIP(request));
        logininforEvent.setUserAgent(request.getHeader("User-Agent"));
        SpringUtils.context().publishEvent(logininforEvent);
    }

    /**
     * 校验短信验证码
     */
    private boolean validateSmsCode(String phonenumber, String smsCode) {
        String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + phonenumber);
        if (StringUtils.isBlank(code)) {
            recordLogininfor(phonenumber, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        return code.equals(smsCode);
    }

    /**
     * 校验邮箱验证码
     */
    private boolean validateEmailCode(String email, String emailCode) {
        String code = RedisUtils.getCacheObject(CacheConstants.CAPTCHA_CODE_KEY + email);
        if (StringUtils.isBlank(code)) {
            recordLogininfor(email, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        return code.equals(emailCode);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    private SysUserVo loadUserByUsername(String username) {
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username));
        return checkLoadedUser(user, username);
    }

    private SysUserVo loadUserByUsernameOrEmail(String username) {
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username), false);
        if (ObjectUtil.isNull(user)) {
            user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, username), false);
        }
        return checkLoadedUser(user, username);
    }

    private SysUserVo checkLoadedUser(SysUserVo user, String username) {
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return user;
    }

    private SysUserVo loadUserByPhonenumber(String phonenumber) {
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhonenumber, phonenumber));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", phonenumber);
            throw new UserException("user.not.exists", phonenumber);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", phonenumber);
            throw new UserException("user.blocked", phonenumber);
        }
        return user;
    }

    private SysUserVo loadUserByEmail(String email) {
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", email);
            throw new UserException("user.not.exists", email);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", email);
            throw new UserException("user.blocked", email);
        }
        return user;
    }

    private SysUserVo loadUserByOpenid(String openid) {
        // 使用 openid 查询绑定用户 如未绑定用户 则根据业务自行处理 例如 创建默认用户
        // todo 自行实现 userService.selectUserByOpenid(openid);
        SysUserVo user = new SysUserVo();
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", openid);
            // todo 用户不存在 业务逻辑自行实现
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", openid);
            // todo 用户已被停用 业务逻辑自行实现
        }
        return user;
    }

    /**
     * 构建登录用户
     */
    private LoginUser buildLoginUser(SysUserVo user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setTenantId(user.getTenantId());
        setTenantIdentity(loginUser);
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());
        TenantContextHolder.runWithTenant(user.getTenantId(), () -> {
            loginUser.setMenuPermission(permissionService.getMenuPermission(user.getUserId()));
            loginUser.setRolePermission(permissionService.getRolePermission(user.getUserId()));

            SysDeptVo dept = null;
            if (ObjectUtil.isNotNull(user.getDeptId())) {
                dept = deptService.selectDeptById(user.getDeptId());
            }
            loginUser.setDeptName(ObjectUtil.isNull(dept) ? "" : dept.getDeptName());
            List<SysRoleVo> roles = roleService.selectRolesByUserId(user.getUserId());
            loginUser.setRoles(BeanUtil.copyToList(roles, RoleVO.class));
        });

        return loginUser;
    }

    private void setTenantIdentity(LoginUser loginUser) {
        if (loginUser.getTenantId() == null) {
            throw ServiceException.ofMessageKey("tenant.context.missing");
        }
        SysTenant tenant = tenantMapper.selectById(loginUser.getTenantId());
        if (tenant == null) {
            throw ServiceException.ofMessageKey("tenant.context.missing");
        }
        loginUser.setTenantType(tenant.getTenantType());
        if (TenantType.MERCHANT.getCode().equals(tenant.getTenantType())) {
            MerchantProfileVo profile = merchantProfileService.selectByTenantId(tenant.getTenantId());
            if (profile != null) {
                loginUser.setMerchantId(profile.getMerchantId());
            }
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId, String username) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ServletUtils.getClientIP());
        sysUser.setLoginDate(TimeUtils.utcNow());
        sysUser.setUpdateBy(username);
        userMapper.updateById(sysUser);
    }

    /**
     * 登录校验
     */
    private void checkLogin(LoginType loginType, String username, Supplier<Boolean> supplier) {
        String clientIP = ServletUtils.getClientIP();
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + username+":"+clientIP;
        String loginFail = Constants.LOGIN_FAIL;

        // 获取用户登录错误次数，默认为0 (可自定义限制策略 例如: key + username + ip)
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);
        // 锁定时间内登录 则踢出
        if (errorNumber >= maxRetryCount) {
            recordLogininfor(username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
            throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
        }

        if (supplier.get()) {
            // 错误次数递增
            errorNumber++;
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
            // 达到规定错误次数 则锁定登录
            if (errorNumber >= maxRetryCount) {
                recordLogininfor(username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
                throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
            } else {
                // 未达到规定错误次数
                recordLogininfor(username, loginFail, MessageUtils.message(loginType.getRetryLimitCount(), errorNumber));
                throw new UserException(loginType.getRetryLimitCount(), errorNumber);
            }
        }

        // 登录成功 清空错误次数
        RedisUtils.deleteObject(errorKey);
    }
}
