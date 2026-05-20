package com.bocoo.common.web.filter;

import com.bocoo.common.core.config.properties.TenantProperties;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
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
            String tenantId = httpRequest.getHeader(TenantConstants.TENANT_ID_HEADER);
            if (StringUtils.isBlank(tenantId)) {
                tenantId = TenantConstants.DEFAULT_TENANT_ID;
            }
            TenantContextHolder.setTenantId(Long.valueOf(tenantId));
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    public TenantProperties getProperties() {
        return properties;
    }
}
