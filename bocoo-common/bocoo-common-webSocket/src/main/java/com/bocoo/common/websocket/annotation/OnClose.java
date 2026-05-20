package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket连接关闭处理注解
 *
 * 该注解用于标记处理WebSocket连接关闭事件的方法。
 * 当WebSocket连接断开或关闭时，会自动调用被此注解标记的方法。
 * 通常用于执行资源清理、会话移除、在线人数统计更新等操作。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnClose {
}
