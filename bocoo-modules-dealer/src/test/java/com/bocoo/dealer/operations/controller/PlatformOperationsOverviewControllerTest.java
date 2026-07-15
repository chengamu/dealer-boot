package com.bocoo.dealer.operations.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.operations.domain.bo.OperationsMerchantQueryBo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantLevelOptionVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantVo;
import com.bocoo.dealer.operations.domain.vo.OperationsSummaryVo;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformOperationsOverviewControllerTest {

    @Test
    void operationsEndpointsUsePlatformDashboardPermissionAndDedicatedPaths() throws Exception {
        assertThat(PlatformOperationsOverviewController.class.getAnnotation(RequestMapping.class).value())
            .containsExactly("/platform-sales/operations");

        assertOperation("summary", new Class<?>[0], "/summary", R.class, OperationsSummaryVo.class);
        assertOperation("merchants", new Class<?>[]{OperationsMerchantQueryBo.class, PageQuery.class},
            "/merchants", TableDataInfo.class, OperationsMerchantVo.class);
        assertOperation("levels", new Class<?>[]{String.class}, "/levels/options", R.class,
            OperationsMerchantLevelOptionVo.class);
        assertOperation("applications", new Class<?>[]{OperationsMerchantQueryBo.class, PageQuery.class},
            "/applications", TableDataInfo.class, OperationsMerchantVo.class);
    }

    private void assertOperation(String methodName, Class<?>[] parameterTypes, String path,
                                 Class<?> returnType, Class<?> genericType) throws Exception {
        Method method = PlatformOperationsOverviewController.class.getMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission.value()).containsExactly("platform:sales:dashboard:view");
        assertThat(method.getReturnType()).isEqualTo(returnType);
        assertThat(method.getGenericReturnType().getTypeName()).contains(genericType.getSimpleName());
        assertThat(method.getAnnotation(GetMapping.class).value()).containsExactly(path);
    }
}
