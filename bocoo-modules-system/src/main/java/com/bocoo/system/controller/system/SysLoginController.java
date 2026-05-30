package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.constant.Constants;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.domain.bo.EmailLoginBody;
import com.bocoo.common.core.domain.bo.LoginBody;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.domain.bo.SmsLoginBody;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.ratelimiter.annotation.RateLimiter;
import com.bocoo.common.ratelimiter.enums.LimitType;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.bo.ThirdLoginBody;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.vo.RouterVo;
import com.bocoo.system.domain.vo.SysUserVo;
import com.bocoo.system.service.SysLoginService;
import com.bocoo.system.service.SysMenuService;
import com.bocoo.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录验证
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@Tag(name = "系统登录", description = "系统登录验证接口")
public class SysLoginController {

    private final SysLoginService loginService;
    private final SysMenuService menuService;
    private final SysUserService userService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @PostMapping("/login")
    @Operation(summary = "账号密码登录", description = "使用用户名和密码进行登录")
    public R<Map<String, Object>> login(
            @Parameter(description = "登录信息", required = true)
            @Validated @RequestBody LoginBody loginBody) {
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        return R.ok(MessageUtils.message("auth.login.success"), buildLoginResponse(token));
    }


    /**
     * 第三方登录接口
     * 通过appKey和secretKey进行身份验证，生成并返回访问令牌
     *
     * @param loginBody 登录信息对象，包含appKey和secretKey，不能为空
     * @return 包含访问令牌的响应结果，令牌存储在Constants.TOKEN键下
     */
    @SaIgnore
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @PostMapping("/thirdLogin")
    @Operation(summary = "第三方登录", description = "使用appKey和secretKey进行登录")
    public R<Map<String, Object>> thirdLogin(
            @Parameter(description = "登录信息", required = true)
            @Validated @RequestBody ThirdLoginBody loginBody) {
        // 生成令牌
        String token = loginService.thirdLogin(loginBody.getAppKey(), loginBody.getSecretKey());
        return R.ok(MessageUtils.message("auth.login.success"), buildLoginResponse(token));
    }



    /**
     * 短信登录
     *
     * @param smsLoginBody 登录信息
     * @return 结果
     */
    @SaIgnore
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @PostMapping("/smsLogin")
    @Operation(summary = "短信登录", description = "使用手机号和短信验证码进行登录")
    public R<Map<String, Object>> smsLogin(
            @Parameter(description = "短信登录信息", required = true)
            @Validated @RequestBody SmsLoginBody smsLoginBody) {
        // 生成令牌
        String token = loginService.smsLogin(smsLoginBody.getPhonenumber(), smsLoginBody.getSmsCode());
        return R.ok(MessageUtils.message("auth.login.success"), buildLoginResponse(token));
    }

    /**
     * 邮件登录
     *
     * @param body 登录信息
     * @return 结果
     */
    @SaIgnore
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @PostMapping("/emailLogin")
    @Operation(summary = "邮件登录", description = "使用邮箱和邮件验证码进行登录")
    public R<Map<String, Object>> emailLogin(
            @Parameter(description = "邮件登录信息", required = true)
            @Validated @RequestBody EmailLoginBody body) {
        // 生成令牌
        String token = loginService.emailLogin(body.getEmail(), body.getEmailCode());
        return R.ok(MessageUtils.message("auth.login.success"), buildLoginResponse(token));
    }

    /**
     * 小程序登录(示例)
     *
     * @param xcxCode 小程序code
     * @return 结果
     */
    @SaIgnore
    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @PostMapping("/xcxLogin")
    @Operation(summary = "小程序登录", description = "使用小程序code进行登录")
    public R<Map<String, Object>> xcxLogin(
            @Parameter(description = "小程序code", required = true)
            @NotBlank(message = "{xcx.code.not.blank}") String xcxCode) {
        // 生成令牌
        String token = loginService.xcxLogin(xcxCode);
        return R.ok(MessageUtils.message("auth.login.success"), buildLoginResponse(token));
    }

    /**
     * 退出登录
     */
    @SaIgnore
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "退出当前登录状态")
    public R<Void> logout() {
        loginService.logout();
        return R.ok(MessageUtils.message("common.logout.success"));
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return TenantContextHolder.callWithTenant(loginUser.getTenantId(), () -> {
            SysUserVo user = userService.selectUserById(loginUser.getUserId());
            Map<String, Object> info = new HashMap<>();
            info.put("user", user);
            info.put("roles", loginUser.getRolePermission());
            info.put("permissions", loginUser.getMenuPermission());
            info.put("tenantId", loginUser.getTenantId());
            info.put("tenantType", loginUser.getTenantType());
            info.put("merchantId", loginUser.getMerchantId());
            info.put("forcePasswordChange", "1".equals(user.getForcePasswordChange()));
            return R.ok(info);
        });
    }

    private Map<String, Object> buildLoginResponse(String token) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return TenantContextHolder.callWithTenant(loginUser.getTenantId(), () -> {
            SysUserVo user = userService.selectUserById(loginUser.getUserId());
            return Map.of(
                Constants.TOKEN, token,
                "forcePasswordChange", "1".equals(user.getForcePasswordChange())
            );
        });
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    @Operation(summary = "获取路由信息", description = "获取当前用户的菜单路由信息")
    public R<List<RouterVo>> getRouters() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return TenantContextHolder.callWithTenant(loginUser.getTenantId(), () -> {
            List<SysMenu> menus = menuService.selectMenuTreeByUserId(loginUser.getUserId());
            List<RouterVo> routerVos = menuService.buildMenus(menus);
            return R.ok(menuService.resetChildrenName(routerVos));
        });
    }
}
