package com.bocoo.common.websocket.annotation;

import com.bocoo.common.websocket.standard.ServerEndpointExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Netty WebSocket自动配置选择器
 *
 * 该配置类用于自动配置Netty WebSocket相关组件。
 * 当应用上下文中不存在ServerEndpointExporter Bean时，
 * 会自动创建并注册一个ServerEndpointExporter实例。
 *
 * <p>主要功能：
 * <ul>
 *   <li>条件化注册ServerEndpointExporter Bean</li>
 *   <li>确保WebSocket端点能够被正确扫描和注册</li>
 *   <li>提供Netty WebSocket服务的基础设施支持</li>
 * </ul>
 *
 * @author bocoo
 * @since 1.0.0
 * @see ServerEndpointExporter
 * @see EnableWebSocket
 */
@ConditionalOnMissingBean(ServerEndpointExporter.class)
@Configuration
public class NettyWebSocketSelector {

    /**
     * 创建并注册ServerEndpointExporter Bean
     *
     * ServerEndpointExporter负责扫描并注册所有标记了@ServerEndpoint注解的WebSocket端点类。
     * 它会处理端点类的生命周期管理、URL映射、端口绑定等核心功能。
     *
     * <p>该Bean的创建条件：
     * <ul>
     *   <li>当应用上下文中不存在同名Bean时才会创建</li>
     *   <li>确保不会与用户自定义的配置冲突</li>
     * </ul>
     *
     * @return ServerEndpointExporter实例
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
