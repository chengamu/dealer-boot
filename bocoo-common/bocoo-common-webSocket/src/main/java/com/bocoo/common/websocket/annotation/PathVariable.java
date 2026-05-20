package com.bocoo.common.websocket.annotation;

import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket路径变量绑定注解
 *
 * 该注解用于将WebSocket端点URL中的路径变量绑定到方法参数上。
 * 支持RESTful风格的URL参数提取，允许从WebSocket连接URL中获取路径参数。
 * 通常与@ServerEndpoint注解配合使用，实现动态路径参数解析。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {

    /**
     * 路径变量名称的别名
     *
     * 与name()属性互为别名，可以互换使用。
     * 用于指定需要绑定的路径变量的名称。
     *
     * @return 路径变量名称
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 需要绑定的路径变量名称
     *
     * 与value()属性互为别名，可以互换使用。
     * 指定URL路径中对应的变量名称，用于参数绑定。
     *
     * @return 路径变量名称
     */
    @AliasFor("value")
    String name() default "";

}
