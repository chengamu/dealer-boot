package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端 history 路由入口。
 *
 * @author CMX
 */
@Controller
public class SpaIndexController {

    @SaIgnore
    @GetMapping({
        "/index",
        "/login",
        "/register",
        "/merchant/apply",
        "/product-master/{*path}",
        "/product-config/{*path}",
        "/product-release/{*path}"
    })
    public String index() {
        return "forward:/index.html";
    }
}
