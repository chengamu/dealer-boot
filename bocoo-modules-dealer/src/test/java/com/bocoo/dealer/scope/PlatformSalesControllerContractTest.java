package com.bocoo.dealer.scope;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.dealer.controller.PlatformSalesDocumentQueryController;
import com.bocoo.dealer.quickorder.controller.PlatformQuickOrderQueryController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformSalesControllerContractTest {
    @Test
    void platformControllersUseDedicatedPathsAndPermissions() throws NoSuchMethodException {
        assertContract(PlatformQuickOrderQueryController.class, "/platform-sales/quick-orders",
            "platform:sales:quick-order:list", "platform:sales:quick-order:query");
        assertContract(PlatformSalesDocumentQueryController.class, "/platform-sales/orders",
            "platform:sales:order:list", "platform:sales:order:query");
        assertThat(PlatformQuickOrderQueryController.class.getMethod("export",
            com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo.class,
            jakarta.servlet.http.HttpServletResponse.class).getAnnotation(SaCheckPermission.class).value())
            .containsExactly("platform:sales:quick-order:export");
        assertThat(PlatformSalesDocumentQueryController.class.getMethod("pdf", Long.class,
            jakarta.servlet.http.HttpServletResponse.class).getAnnotation(SaCheckPermission.class).value())
            .containsExactly("platform:sales:order:document");
        assertThat(PlatformSalesDocumentQueryController.class.getMethod("export", Long.class,
            jakarta.servlet.http.HttpServletResponse.class).getAnnotation(SaCheckPermission.class).value())
            .containsExactly("platform:sales:order:export");
    }

    private void assertContract(Class<?> type, String path, String listPermission,
                                String queryPermission) throws NoSuchMethodException {
        assertThat(type.getAnnotation(RequestMapping.class).value()).containsExactly(path);
        assertThat(type.getMethod("list", type == PlatformQuickOrderQueryController.class
            ? com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo.class
            : com.bocoo.dealer.domain.bo.SalesDocumentBo.class,
            com.bocoo.common.mybatis.core.page.PageQuery.class)
            .getAnnotation(SaCheckPermission.class).value()).containsExactly(listPermission);
        assertThat(type.getMethod("get", Long.class).getAnnotation(SaCheckPermission.class).value())
            .containsExactly(queryPermission);
    }
}
