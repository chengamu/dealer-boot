package com.bocoo.common.websocket.exception;

import java.io.Serial;

/**
 * WebSocket部署异常类
 *
 * 该异常类用于表示WebSocket端点部署过程中发生的错误。
 * 当WebSocket服务器在注册、配置或启动端点时遇到问题，
 * 会抛出此异常来标识部署失败。
 *
 * <p>常见触发场景包括：
 * <ul>
 *   <li>端点配置参数无效</li>
 *   <li>端点类注解配置错误</li>
 *   <li>端口绑定失败</li>
 *   <li>SSL证书配置错误</li>
 *   <li>服务器启动参数异常</li>
 * </ul>
 *
 * @author bocoo
 * @since 1.0.0
 */
public class DeploymentException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造包含指定详细消息的部署异常
     *
     * @param message 详细错误信息
     */
    public DeploymentException(String message) {
        super(message);
    }

    /**
     * 构造包含指定详细消息和原因的部署异常
     *
     * @param message 详细错误信息
     * @param cause 导致此异常的原因
     */
    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
