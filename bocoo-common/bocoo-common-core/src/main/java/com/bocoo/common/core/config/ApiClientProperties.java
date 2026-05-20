package com.bocoo.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ApiClient 配置属性类，与 application.yml 中 apiclient 节点绑定
 * 提供日志开关、响应体限制、重试策略、线程池、超时、连接池等配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "apiclient")
public class ApiClientProperties {

    /** 是否启用日志记录 */
    private boolean enableLogging = true;

    /** 最大响应体大小（字节），默认 10MB */
    private long maxResponseSize = 10 * 1024 * 1024;

    /** 最大重试次数 */
    private int maxRetries = 3;

    /** 基础重试间隔（毫秒） */
    private long baseRetryIntervalMs = 200;

    /** 最大重试间隔（毫秒） */
    private long maxRetryIntervalMs = 5000;

    /** 线程池核心线程数 */
    private int corePoolSize = 10;

    /** 线程池最大线程数 */
    private int maxPoolSize = 50;

    /** 线程池队列容量 */
    private int queueCapacity = 5000;

    /** 线程闲置回收时间（秒） */
    private int keepAliveSeconds = 60;

    /** 连接超时（秒） */
    private int connectTimeoutSec = 5;

    /** 读取超时（秒） */
    private int readTimeoutSec = 10;

    /** 写入超时（秒） */
    private int writeTimeoutSec = 10;

    /** 连接池最大空闲连接数 */
    private int maxIdleConnections = 100;

    /** 连接池保持活动时间（分钟） */
    private int keepAliveMinutes = 10;

}
