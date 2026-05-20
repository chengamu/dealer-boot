package com.bocoo.common.doc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Knife4j整合Swagger3 Api接口文档配置类
 *
 * @author cmx
 */
@Configuration
public class Knife4jConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth"; // 改回 BearerAuth

    @Autowired
    private ServerProperties serverProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")  // 明确指定 header 名称
                                        .description("JWT Authorization header using the Bearer scheme. Example: \"Bearer {token}\"")))
                .info(new Info()
                        .title("开发内部API接口文档")
                        .description("基于SpringBoot和RuoYi框架的后端接口文档")
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Knife4j加载成功后触发banner显示
     */
    @PostConstruct
    public void banner() {
        Integer port = serverProperties.getPort();
        String contextPath = serverProperties.getServlet().getContextPath();
        if (port == null) {
            port = 8080; // 默认端口
        }
        if (contextPath == null) {
            contextPath = ""; // 默认上下文路径
        }

        System.out.printf("------------------------------------------------- 启用knife4j api文档插件 -----------------------------------------------\r\n" +
                        "                              knife4j访问地址：http://127.0.0.1:%s%s%s                                           \r\n" +
                        "                       smart-doc访问地址：http://127.0.0.1:%s%s%s                              \r\n" +
                        "-----------------------------------------------------------------------------------------------------------------------%n",
                port, contextPath, "/doc.html", port, contextPath, "/static/doc/index.html");
    }
}
