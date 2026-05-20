package com.bocoo.common.websocket.annotation;

import org.springframework.context.annotation.Import;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用Netty WebSocket功能的注解
 *
 * 该注解用于在Spring应用中启用基于Netty的WebSocket支持。
 * 类似于Spring Boot的@Enable*系列注解，用于激活特定功能模块。
 *
 * <p>使用方式：
 * <pre>
 * &#064;Configuration
 * &#064;EnableWebSocket
 * public class WebSocketConfig {
 *     // 配置内容
 * }
 * </pre>
 *
 * @author bocoo
 * @since 1.0.0
 * @see NettyWebSocketSelector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(NettyWebSocketSelector.class)
public @interface EnableWebSocket {

    /**
     * 指定WebSocket端点类的扫描基础包路径
     *
     * 用于自定义WebSocket端点类的包扫描路径。
     * 如果不指定，则使用默认的组件扫描机制。
     * 支持配置多个包路径。
     *
     * <p>使用示例：
     * <pre>
     * &#064;EnableWebSocket(scanBasePackages = {"com.example.ws", "com.myapp.websocket"})
     * </pre>
     *
     * @return 需要扫描的包路径数组，默认为空数组
     */
    String[] scanBasePackages() default {};

}
