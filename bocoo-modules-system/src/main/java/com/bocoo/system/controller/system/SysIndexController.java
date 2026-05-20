package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.config.BocooConfig;
import com.bocoo.common.core.utils.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author CMX
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "系统首页", description = "系统首页接口")
public class SysIndexController {

    /**
     * 系统基础配置
     */
    private final BocooConfig bocooConfig;

    /**
     * 访问首页，提示语
     */
    @SaIgnore
    @GetMapping("/")
    @Operation(summary = "访问首页", description = "访问系统首页，返回欢迎信息")
    public String index() {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", bocooConfig.getName(), bocooConfig.getVersion());
    }
}
