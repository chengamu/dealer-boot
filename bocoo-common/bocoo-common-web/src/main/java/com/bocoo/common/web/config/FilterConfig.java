package com.bocoo.common.web.config;

import com.bocoo.common.core.config.properties.TenantProperties;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.web.config.properties.XssProperties;
import com.bocoo.common.web.filter.RepeatableFilter;
import com.bocoo.common.web.filter.TenantContextFilter;
import com.bocoo.common.web.filter.XssFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Filter配置
 *
 * @author Lion Li
 */
@AutoConfiguration
@EnableConfigurationProperties({XssProperties.class, TenantProperties.class})
public class FilterConfig {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @ConditionalOnProperty(prefix = "bocoo.tenant", name = "enabled", havingValue = "true")
    public FilterRegistrationBean tenantContextFilterRegistration(TenantProperties tenantProperties) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new TenantContextFilter(tenantProperties));
        registration.addUrlPatterns("/*");
        registration.setName("tenantContextFilter");
        registration.setOrder(FilterRegistrationBean.LOWEST_PRECEDENCE - 100);
        return registration;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    public FilterRegistrationBean xssFilterRegistration(XssProperties xssProperties) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns(StringUtils.split(xssProperties.getUrlPatterns(), StringUtils.SEPARATOR));
        registration.setName("xssFilter");
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("excludes", xssProperties.getExcludes());
        registration.setInitParameters(initParameters);
        return registration;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatableFilter());
        registration.addUrlPatterns("/*");
        registration.setName("repeatableFilter");
        registration.setOrder(FilterRegistrationBean.LOWEST_PRECEDENCE);
        return registration;
    }

}
