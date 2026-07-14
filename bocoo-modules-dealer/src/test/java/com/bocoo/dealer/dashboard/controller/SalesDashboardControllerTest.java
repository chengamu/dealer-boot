package com.bocoo.dealer.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.dealer.dashboard.domain.SalesDashboardVo;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
class SalesDashboardControllerTest {
    @Test
    void businessEndpointUsesBusinessServiceAndPermission() throws Exception {
        assertContract(SalesDashboardController.class, "/sales/dashboard", "sales:dashboard:view");
    }

    @Test
    void platformEndpointIsIndependentAndUsesPlatformPermission() throws Exception {
        assertContract(PlatformSalesDashboardController.class, "/platform-sales/dashboard",
            "platform:sales:dashboard:view");
    }

    private void assertContract(Class<?> controllerType, String path, String permission) throws Exception {
        RequestMapping mapping = controllerType.getAnnotation(RequestMapping.class);
        Method get = controllerType.getMethod("get");
        SaCheckPermission check = get.getAnnotation(SaCheckPermission.class);
        assertThat(mapping.value()).containsExactly(path);
        assertThat(check.value()).containsExactly(permission);
        assertThat(get.getReturnType().getTypeName()).contains("R");
        assertThat(get.getGenericReturnType().getTypeName()).contains(SalesDashboardVo.class.getSimpleName());
    }
}
