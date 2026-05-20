package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket异常处理注解
 *
 * 该注解用于标记处理WebSocket连接异常事件的方法。
 * 当WebSocket连接过程中发生异常或错误时，会自动调用被此注解标记的方法。
 * 通常用于记录错误日志、异常处理、连接状态维护等操作。
 *
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnError {
}
