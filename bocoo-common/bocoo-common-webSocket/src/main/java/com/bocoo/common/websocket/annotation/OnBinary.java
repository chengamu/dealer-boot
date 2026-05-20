package com.bocoo.common.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket二进制消息处理注解
 *
 * 该注解用于标记处理WebSocket二进制消息的方法。
 * 当WebSocket连接接收到二进制数据时，会自动调用被此注解标记的方法。
 *
 * <p>使用场景：
 * <ul>
 *   <li>处理图片、音频、视频等二进制数据传输</li>
 *   <li>处理自定义二进制协议数据</li>
 *   <li>高性能数据传输场景</li>
 * </ul>
 *
 * <p>方法签名要求：
 * 被注解的方法可以接受以下参数类型：
 * <ul>
 *   <li>{@code Session} - WebSocket会话对象</li>
 *   <li>{@code byte[]} - 接收到的二进制数据</li>
 * </ul>
 *
 * @author bocoo
 * @since 1.0.0
 * @see com.bocoo.common.websocket.annotation.OnMessage
 * @see com.bocoo.common.websocket.annotation.OnOpen
 * @see com.bocoo.common.websocket.annotation.OnClose
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnBinary {
}
