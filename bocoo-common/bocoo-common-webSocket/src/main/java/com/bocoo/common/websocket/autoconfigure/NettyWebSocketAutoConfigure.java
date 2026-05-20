package com.bocoo.common.websocket.autoconfigure;

import com.bocoo.common.websocket.annotation.EnableWebSocket;

/**
 * Netty WebSocket自动配置类
 *
 * 该类用于自动启用和配置Netty WebSocket功能。
 * 通过使用@EnableWebSocket注解，自动导入WebSocket相关的配置和组件，
 * 实现WebSocket服务的自动装配和启动。
 *
 * <p>该配置类会在Spring Boot应用启动时自动加载，
 * 无需手动配置即可启用WebSocket功能。
 *
 * @author bocoo
 * @since 1.0.0
 * @see EnableWebSocket
 */
@EnableWebSocket
public class NettyWebSocketAutoConfigure {
}
