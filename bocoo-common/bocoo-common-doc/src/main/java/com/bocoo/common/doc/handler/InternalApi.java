package com.bocoo.common.doc.handler;

import java.lang.annotation.*;

/**
 * @author cmx
 * @version 1.0
 * @description: 标记内部API接口（不生成文档）
 * @date 2025/7/23 14:33
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {
    String value() default "";
}
