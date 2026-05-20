package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket事件处理注解
 *
 * 该注解用于标记处理WebSocket底层事件的方法。
 * 当WebSocket连接中发生底层Netty事件时，会自动调用被此注解标记的方法。
 * 主要用于处理心跳检测、连接空闲、网络状态变化等底层事件。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnEvent {
}
