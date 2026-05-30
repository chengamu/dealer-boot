package com.bocoo.common.web.config;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.web.config.properties.CorsProperties;
import com.bocoo.common.web.handler.GlobalExceptionHandler;
import com.bocoo.common.web.interceptor.PlusWebInvokeTimeInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;

/**
 * 通用配置
 *
 * @author Lion Li
 */
@AutoConfiguration
@EnableConfigurationProperties(CorsProperties.class)
public class ResourcesConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public ResourcesConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new UtcLocalDateTimeFormatter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全局访问性能拦截
        registry.addInterceptor(new PlusWebInvokeTimeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //配置 knife4j 的静态资源请求映射地址
        registry.addResourceHandler("/doc.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(Boolean.TRUE.equals(corsProperties.getAllowCredentials()));
        addAllowedOrigins(config, corsProperties.getAllowedOrigins(), false);
        addAllowedOrigins(config, corsProperties.getAllowedOriginPatterns(), true);
        // 设置访问源请求头
        corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);
        // 设置访问源请求方法
        corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
        // 有效期 1800秒
        config.setMaxAge(corsProperties.getMaxAge());
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }

    private void addAllowedOrigins(CorsConfiguration config, List<String> origins, boolean pattern) {
        boolean allowCredentials = Boolean.TRUE.equals(corsProperties.getAllowCredentials());
        for (String origin : origins) {
            if (allowCredentials && "*".equals(origin)) {
                throw new IllegalStateException("CORS wildcard origin is not allowed when credentials are enabled");
            }
            if (pattern) {
                config.addAllowedOriginPattern(origin);
            } else {
                config.addAllowedOrigin(origin);
            }
        }
    }

    /**
     * 全局异常处理器
     * 同一个模块中，GlobalExceptionHandler 这个类本身也被 @RestControllerAdvice (或 @Component 等)注解了，被 Spring 自动扫描到。
     */
/*    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }*/

    private static final class UtcLocalDateTimeFormatter implements Formatter<LocalDateTime> {

        @Override
        public LocalDateTime parse(String text, Locale locale) throws ParseException {
            try {
                return TimeUtils.parseUtcIso(text);
            } catch (RuntimeException e) {
                ParseException parseException = new ParseException("Expected ISO-8601 UTC instant", 0);
                parseException.initCause(e);
                throw parseException;
            }
        }

        @Override
        public String print(LocalDateTime object, Locale locale) {
            return TimeUtils.formatUtcIso(object);
        }
    }
}
