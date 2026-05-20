package com.bocoo.common.websocket.annotation;

import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket请求参数绑定注解
 *
 * 该注解用于将WebSocket连接请求中的查询参数绑定到方法参数上。
 * 支持从WebSocket握手请求的URL参数中提取值，并绑定到对应的方法参数。
 * 提供参数必要性控制和默认值设置功能。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    /**
     * 请求参数名称的别名
     *
     * 与name()属性互为别名，可以互换使用。
     * 用于指定需要绑定的请求参数的名称。
     *
     * @return 请求参数名称
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 需要绑定的请求参数名称
     *
     * 与value()属性互为别名，可以互换使用。
     * 指定HTTP请求参数中对应的参数名称，用于参数绑定。
     *
     * @return 请求参数名称
     */
    @AliasFor("value")
    String name() default "";

    /**
     * 指定参数是否为必需
     *
     * 默认值为true，表示参数是必需的，如果请求中缺少该参数将抛出异常。
     * 设置为false时，如果请求中没有该参数则使用null值。
     * 如果提供了defaultValue()，则此属性隐式设置为false。
     *
     * @return 参数是否必需
     */
    boolean required() default true;

    /**
     * 当请求参数未提供或为空时使用的默认值
     *
     * 提供默认值时，会隐式将required()属性设置为false。
     * 用于在参数缺失时提供备选值，避免出现null值或异常。
     *
     * @return 参数默认值
     */
    String defaultValue() default "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";

}
