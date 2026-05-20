package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket握手前处理注解
 *
 * 该注解用于标记在WebSocket握手建立之前需要执行的方法。
 * 被此注解标记的方法将在WebSocket连接建立前被调用，
 * 可用于进行身份验证、参数校验或连接拒绝等操作。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeHandshake {
}
