package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket消息处理注解
 *
 * 该注解用于标记处理WebSocket文本消息的方法。
 * 当WebSocket连接接收到文本消息时，会自动调用被此注解标记的方法。
 * 通常用于处理客户端发送的字符串消息、命令解析、业务逻辑处理等操作。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnMessage {
}
