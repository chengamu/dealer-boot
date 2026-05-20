package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket连接建立处理注解
 *
 * 该注解用于标记处理WebSocket连接建立事件的方法。
 * 当新的WebSocket连接成功建立时，会自动调用被此注解标记的方法。
 * 通常用于执行用户身份验证、会话初始化、在线状态更新等操作。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnOpen {
}
