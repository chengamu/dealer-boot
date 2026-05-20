package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.domain.bo.RegisterBody;
import com.bocoo.system.service.SysConfigService;
import com.bocoo.system.service.SysRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@Tag(name = "用户注册", description = "用户注册接口")
public class SysRegisterController extends BaseController {

    private final SysRegisterService registerService;
    private final SysConfigService configService;

    /**
     * 用户注册
     */
    @SaIgnore
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public R<Void> register(
            @Parameter(description = "注册用户信息", required = true)
            @Validated @RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return R.fail("当前系统没有开启注册功能！");
        }
        registerService.register(user);
        return R.ok();
    }
}
