package com.bocoo.common.core.exception;

import com.bocoo.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * MQTT消息处理业务异常类
 * 用于封装MQTT消息处理过程中发生的业务异常
 *
 * @author cmx
 * @version 1.0
 * @date 2025/8/12
 */
public class MqttMessageHandleException extends BaseException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * MQTT模块标识
     */
    private static final String MODULE = "mqtt";

    /**
     * 构造函数
     * @param code 错误码（对应国际化配置文件中的key）
     * @param args 错误码对应的参数
     */
    public MqttMessageHandleException(String code, Object... args) {
        super(MODULE, code, args, null);
    }

    /**
     * 构造函数
     * @param code 错误码（对应国际化配置文件中的key）
     * @param args 错误码对应的参数
     * @param defaultMessage 默认错误消息
     */
    public MqttMessageHandleException(String code, Object[] args, String defaultMessage) {
        super(MODULE, code, args, defaultMessage);
    }

    /**
     * 构造函数（仅用于自定义默认消息）
     * @param defaultMessage 默认错误消息
     */
    public MqttMessageHandleException(String defaultMessage) {
        super(MODULE, null, null, defaultMessage);
    }
}
