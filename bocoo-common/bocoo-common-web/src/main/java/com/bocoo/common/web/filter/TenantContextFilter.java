package com.bocoo.common.web.filter;

import com.bocoo.common.core.config.properties.TenantProperties;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.satoken.utils.LoginHelper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class TenantContextFilter implements Filter {

    private final TenantProperties properties;

    public TenantContextFilter(TenantProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            if (isIgnored(httpRequest.getRequestURI())) {
                TenantContextHolder.setIgnore(true);
            } else {
                TenantContextHolder.setTenantId(LoginHelper.getTenantId());
            }
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private boolean isIgnored(String uri) {
        return properties.getIgnoreUrls().stream().anyMatch(uri::startsWith);
    }
}
