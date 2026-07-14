package com.bocoo.dealer.fulfillment;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.dealer.fulfillment.controller.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentPermissionFamilyTest {
    @Test
    void businessControllersUseProgressPermissionsOnly() {
        assertPermissionPrefix(List.of(BusinessFulfillmentController.class), "dealer:fulfillment:progress:");
    }

    @Test
    void platformControllersUseAdminPermissionsOnly() {
        assertPermissionPrefix(List.of(PlatformFulfillmentController.class), "dealer:fulfillment:admin:");
    }

    @Test
    void factoryControllersUseFactoryPermissionsOnly() {
        assertPermissionPrefix(List.of(FactoryProductionController.class, FactoryShipmentController.class,
            FactoryTrackingController.class), "dealer:fulfillment:factory:");
    }

    private void assertPermissionPrefix(List<Class<?>> controllers, String prefix) {
        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
                assertThat(permission).as(controller.getSimpleName() + "." + method.getName()).isNotNull();
                assertThat(permission.value()).allSatisfy(value -> assertThat(value).startsWith(prefix));
            }
        }
    }
}
