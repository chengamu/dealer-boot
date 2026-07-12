package com.bocoo.pay.service.impl;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bocoo.common.core.config.properties.TenantProperties;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.mybatis.tenant.TenantDatabaseInterceptor;
import com.bocoo.pay.controller.PayPalWebhookController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayTenantBoundaryTest {

    @AfterEach
    void clearContext() {
        TenantContextHolder.clear();
    }

    @Test
    void actualTenantInterceptorHonorsExplicitIgnoreScope() {
        TenantDatabaseInterceptor interceptor = new TenantDatabaseInterceptor(new TenantProperties());
        TenantContextHolder.setTenantId(1L);

        assertThat(interceptor.ignoreTable("pay_order")).isFalse();
        assertThat(interceptor.getTenantId().toString()).isEqualTo("1");

        TenantContextHolder.runWithIgnore(() ->
            assertThat(interceptor.ignoreTable("pay_order")).isTrue());

        assertThat(TenantContextHolder.getRequiredTenantId()).isEqualTo(1L);
        assertThat(interceptor.ignoreTable("pay_order")).isFalse();
    }

    @Test
    void paypalWebhookControllerIsAnonymous() {
        assertThat(PayPalWebhookController.class.isAnnotationPresent(SaIgnore.class)).isTrue();
    }
}
