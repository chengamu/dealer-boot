package com.bocoo.common.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import com.bocoo.common.core.factory.YmlPropertySourceFactory;
import com.bocoo.common.satoken.core.dao.PlusSaTokenDao;
import com.bocoo.common.satoken.core.service.SaPermissionImpl;
import com.bocoo.common.satoken.handler.SaTokenExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;

/**
 * sa-token 配置
 *
 * @author cmx
 */
@AutoConfiguration
@PropertySource(value = "classpath:common-satoken.yml", factory = YmlPropertySourceFactory.class)
public class SaTokenConfig {

    private static final String DEV_JWT_SECRET = "bocoo-dev-jwt-secret-change-before-prod-2026";

    @Bean
    public StpLogic getStpLogicJwt() {
        // Sa-Token 整合 jwt (简单模式)
        return new StpLogicJwtForSimple();
    }

    /**
     * 权限接口实现(使用bean注入方便用户替换)
     */
    @Bean
    public StpInterface stpInterface() {
        return new SaPermissionImpl();
    }

    /**
     * 自定义dao层存储
     */
    @Bean
    public SaTokenDao saTokenDao() {
        return new PlusSaTokenDao();
    }

    @Bean
    public ApplicationRunner jwtSecretValidator(
        @Value("${sa-token.jwt-secret-key:}") String jwtSecretKey,
        @Value("${spring.profiles.active:}") String activeProfiles) {
        return args -> {
            String secret = jwtSecretKey == null ? "" : jwtSecretKey.trim();
            if (secret.isEmpty() || secret.startsWith("${")) {
                throw new IllegalStateException("sa-token.jwt-secret-key must be configured");
            }
            boolean devProfile = Arrays.stream(activeProfiles.split(","))
                .anyMatch(profile -> "dev".equalsIgnoreCase(profile.trim()));
            if (!devProfile && DEV_JWT_SECRET.equals(secret)) {
                throw new IllegalStateException("Development JWT secret must not be used outside dev profile");
            }
            if (secret.length() < 32) {
                throw new IllegalStateException("sa-token.jwt-secret-key must be at least 32 characters");
            }
        };
    }

}
