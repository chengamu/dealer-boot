package com.bocoo.common.websocket.annotation;

import org.springframework.core.annotation.AliasFor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket服务器端点配置注解
 *
 * 该注解用于标记WebSocket服务器端点类，配置WebSocket服务器的各项参数。
 * 支持配置服务器监听地址、端口、Netty相关参数、SSL配置、CORS跨域等。
 * 是构建WebSocket服务器的核心注解。
 *
 * @author Yeauty
 * @author bocoo
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerEndpoint {

    /**
     * WebSocket服务路径的别名
     *
     * 与path()属性互为别名，用于指定WebSocket服务的访问路径。
     *
     * @return WebSocket服务路径
     */
    @AliasFor("path")
    String value() default "/";

    /**
     * WebSocket服务路径
     *
     * 与value()属性互为别名，用于指定WebSocket服务的访问路径。
     *
     * @return WebSocket服务路径
     */
    @AliasFor("value")
    String path() default "/";

    /**
     * WebSocket服务器监听的主机地址
     *
     * 指定服务器绑定的IP地址，默认为"0.0.0.0"表示监听所有网络接口。
     *
     * @return 主机地址
     */
    String host() default "0.0.0.0";

    /**
     * WebSocket服务器监听端口
     *
     * 指定服务器监听的端口号，默认为"80"。
     *
     * @return 端口号
     */
    String port() default "80";

    /**
     * Boss线程组线程数
     *
     * 指定处理新连接的Boss线程组的线程数量，默认为"1"。
     *
     * @return Boss线程组线程数
     */
    String bossLoopGroupThreads() default "1";

    /**
     * Worker线程组线程数
     *
     * 指定处理IO事件的Worker线程组的线程数量，默认为"0"表示使用默认值。
     *
     * @return Worker线程组线程数
     */
    String workerLoopGroupThreads() default "0";

    /**
     * 是否使用压缩处理器
     *
     * 指定是否启用压缩处理器，默认为"false"。
     *
     * @return 是否使用压缩处理器
     */
    String useCompressionHandler() default "false";

    //------------------------- option -------------------------

    /**
     * 连接超时时间(毫秒)
     *
     * 指定连接超时时间，单位为毫秒，默认为"30000"(30秒)。
     *
     * @return 连接超时时间
     */
    String optionConnectTimeoutMillis() default "30000";

    /**
     * TCP连接请求队列最大长度
     *
     * 指定TCP连接请求的等待队列最大长度，默认为"128"。
     *
     * @return TCP连接请求队列最大长度
     */
    String optionSoBacklog() default "128";

    //------------------------- childOption -------------------------

    /**
     * 写操作自旋计数
     *
     * 指定写操作的最大自旋计数，默认为"16"。
     *
     * @return 写操作自旋计数
     */
    String childOptionWriteSpinCount() default "16";

    /**
     * 写缓冲区高水位标记
     *
     * 指定写缓冲区的高水位标记，默认为"65536"(64KB)。
     *
     * @return 写缓冲区高水位标记
     */
    String childOptionWriteBufferHighWaterMark() default "65536";

    /**
     * 写缓冲区低水位标记
     *
     * 指定写缓冲区的低水位标记，默认为"32768"(32KB)。
     *
     * @return 写缓冲区低水位标记
     */
    String childOptionWriteBufferLowWaterMark() default "32768";

    /**
     * 接收缓冲区大小
     *
     * 指定TCP接收缓冲区大小，-1表示使用系统默认值。
     *
     * @return 接收缓冲区大小
     */
    String childOptionSoRcvbuf() default "-1";

    /**
     * 发送缓冲区大小
     *
     * 指定TCP发送缓冲区大小，-1表示使用系统默认值。
     *
     * @return 发送缓冲区大小
     */
    String childOptionSoSndbuf() default "-1";

    /**
     * 是否启用TCP无延迟
     *
     * 指定是否启用TCP_NODELAY选项，默认为"true"。
     *
     * @return 是否启用TCP无延迟
     */
    String childOptionTcpNodelay() default "true";

    /**
     * 是否启用TCP保持连接
     *
     * 指定是否启用SO_KEEPALIVE选项，默认为"false"。
     *
     * @return 是否启用TCP保持连接
     */
    String childOptionSoKeepalive() default "false";

    /**
     * Socket linger时间
     *
     * 指定Socket linger时间，-1表示使用系统默认值。
     *
     * @return Socket linger时间
     */
    String childOptionSoLinger() default "-1";

    /**
     * 是否允许半关闭
     *
     * 指定是否允许连接的半关闭状态，默认为"false"。
     *
     * @return 是否允许半关闭
     */
    String childOptionAllowHalfClosure() default "false";

    //------------------------- idleEvent -------------------------

    /**
     * 读空闲超时时间(秒)
     *
     * 指定读操作空闲超时时间，单位为秒，默认为"0"表示禁用。
     *
     * @return 读空闲超时时间
     */
    String readerIdleTimeSeconds() default "0";

    /**
     * 写空闲超时时间(秒)
     *
     * 指定写操作空闲超时时间，单位为秒，默认为"0"表示禁用。
     *
     * @return 写空闲超时时间
     */
    String writerIdleTimeSeconds() default "0";

    /**
     * 所有操作空闲超时时间(秒)
     *
     * 指定所有操作空闲超时时间，单位为秒，默认为"0"表示禁用。
     *
     * @return 所有操作空闲超时时间
     */
    String allIdleTimeSeconds() default "0";

    //------------------------- handshake -------------------------

    /**
     * 最大帧载荷长度
     *
     * 指定WebSocket帧的最大载荷长度，默认为"65536"(64KB)。
     *
     * @return 最大帧载荷长度
     */
    String maxFramePayloadLength() default "65536";

    //------------------------- eventExecutorGroup -------------------------

    /**
     * 是否使用事件执行器组
     *
     * 指定是否使用独立的事件执行器组(另一个线程池)来执行耗时的同步业务逻辑，默认为"true"。
     *
     * @return 是否使用事件执行器组
     */
    String useEventExecutorGroup() default "true";

    /**
     * 事件执行器组线程数
     *
     * 指定事件执行器组的线程数量，默认为"16"。
     *
     * @return 事件执行器组线程数
     */
    String eventExecutorGroupThreads() default "16";

    //------------------------- ssl (refer to spring Ssl) -------------------------

    /**
     * SSL密钥密码
     *
     * 指定SSL密钥的密码，默认为空字符串。
     *
     * @return SSL密钥密码
     */
    String sslKeyPassword() default "";

    /**
     * SSL密钥库路径
     *
     * 指定SSL密钥库文件路径，例如"classpath:server.jks"。
     *
     * @return SSL密钥库路径
     */
    String sslKeyStore() default "";

    /**
     * SSL密钥库密码
     *
     * 指定SSL密钥库的密码，默认为空字符串。
     *
     * @return SSL密钥库密码
     */
    String sslKeyStorePassword() default "";

    /**
     * SSL密钥库类型
     *
     * 指定SSL密钥库的类型，例如"JKS"。
     *
     * @return SSL密钥库类型
     */
    String sslKeyStoreType() default "";

    /**
     * SSL信任库路径
     *
     * 指定SSL信任库文件路径。
     *
     * @return SSL信任库路径
     */
    String sslTrustStore() default "";

    /**
     * SSL信任库密码
     *
     * 指定SSL信任库的密码，默认为空字符串。
     *
     * @return SSL信任库密码
     */
    String sslTrustStorePassword() default "";

    /**
     * SSL信任库类型
     *
     * 指定SSL信任库的类型。
     *
     * @return SSL信任库类型
     */
    String sslTrustStoreType() default "";

    //------------------------- cors (refer to spring CrossOrigin) -------------------------

    /**
     * CORS跨域允许的源站
     *
     * 指定允许跨域访问的源站地址数组。
     *
     * @return CORS允许的源站数组
     */
    String[] corsOrigins() default {};

    /**
     * CORS是否允许凭证
     *
     * 指定跨域请求是否允许携带凭证信息。
     *
     * @return CORS是否允许凭证
     */
    String corsAllowCredentials() default "";

}
