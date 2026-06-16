package com.bocoo.product.service;

import com.bocoo.common.core.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * 产品能力实体默认值。
 */
public final class ProductEntityDefaults {

    private ProductEntityDefaults() {
    }

    public static void prepareInsert(Object entity) {
        if (entity == null) {
            return;
        }
        try {
            Method getter = entity.getClass().getMethod("getDelFlag");
            Object value = getter.invoke(entity);
            if (value instanceof String delFlag && StringUtils.isNotBlank(delFlag)) {
                return;
            }
            Method setter = entity.getClass().getMethod("setDelFlag", String.class);
            setter.invoke(entity, "0");
        } catch (ReflectiveOperationException ignored) {
            // Not every product entity has delFlag.
        }
    }
}
