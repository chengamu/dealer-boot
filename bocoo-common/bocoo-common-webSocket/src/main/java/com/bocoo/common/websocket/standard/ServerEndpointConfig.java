package com.bocoo.common.websocket.standard;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * WebSocket服务器端点配置类
 *
 * 该类用于存储和管理WebSocket服务器的各项配置参数，
 * 包括网络配置、Netty通道配置、SSL配置、CORS配置等。
 *
 * @author Yeauty
 * @version 1.0
 */
public class ServerEndpointConfig {

    /**
     * 服务器主机地址
     */
    private final String HOST;

    /**
     * 服务器端口号
     */
    private final int PORT;

    /**
     * Boss线程组线程数
     */
    private final int BOSS_LOOP_GROUP_THREADS;

    /**
     * Worker线程组线程数
     */
    private final int WORKER_LOOP_GROUP_THREADS;

    /**
     * 是否使用压缩处理器
     */
    private final boolean USE_COMPRESSION_HANDLER;

    /**
     * 连接超时时间(毫秒)
     */
    private final int CONNECT_TIMEOUT_MILLIS;

    /**
     * TCP连接请求队列最大长度
     */
    private final int SO_BACKLOG;

    /**
     * 写操作自旋计数
     */
    private final int WRITE_SPIN_COUNT;

    /**
     * 写缓冲区高水位标记
     */
    private final int WRITE_BUFFER_HIGH_WATER_MARK;

    /**
     * 写缓冲区低水位标记
     */
    private final int WRITE_BUFFER_LOW_WATER_MARK;

    /**
     * 接收缓冲区大小
     */
    private final int SO_RCVBUF;

    /**
     * 发送缓冲区大小
     */
    private final int SO_SNDBUF;

    /**
     * 是否启用TCP无延迟
     */
    private final boolean TCP_NODELAY;

    /**
     * 是否启用TCP保持连接
     */
    private final boolean SO_KEEPALIVE;

    /**
     * Socket linger时间
     */
    private final int SO_LINGER;

    /**
     * 是否允许半关闭
     */
    private final boolean ALLOW_HALF_CLOSURE;

    /**
     * 读空闲超时时间(秒)
     */
    private final int READER_IDLE_TIME_SECONDS;

    /**
     * 写空闲超时时间(秒)
     */
    private final int WRITER_IDLE_TIME_SECONDS;

    /**
     * 所有操作空闲超时时间(秒)
     */
    private final int ALL_IDLE_TIME_SECONDS;

    /**
     * 最大帧载荷长度
     */
    private final int MAX_FRAME_PAYLOAD_LENGTH;

    /**
     * 是否使用事件执行器组
     */
    private final boolean USE_EVENT_EXECUTOR_GROUP;

    /**
     * 事件执行器组线程数
     */
    private final int EVENT_EXECUTOR_GROUP_THREADS;

    /**
     * SSL密钥密码
     */
    private final String KEY_PASSWORD;

    /**
     * SSL密钥库路径
     */
    private final String KEY_STORE;

    /**
     * SSL密钥库密码
     */
    private final String KEY_STORE_PASSWORD;

    /**
     * SSL密钥库类型
     */
    private final String KEY_STORE_TYPE;

    /**
     * SSL信任库路径
     */
    private final String TRUST_STORE;

    /**
     * SSL信任库密码
     */
    private final String TRUST_STORE_PASSWORD;

    /**
     * SSL信任库类型
     */
    private final String TRUST_STORE_TYPE;

    /**
     * CORS跨域允许的源站
     */
    private final String[] CORS_ORIGINS;

    /**
     * CORS是否允许凭证
     */
    private final Boolean CORS_ALLOW_CREDENTIALS;

    /**
     * 随机端口号缓存
     */
    private static Integer randomPort;

    /**
     * 构造函数，初始化服务器端点配置
     *
     * @param host 服务器主机地址
     * @param port 服务器端口号
     * @param bossLoopGroupThreads Boss线程组线程数
     * @param workerLoopGroupThreads Worker线程组线程数
     * @param useCompressionHandler 是否使用压缩处理器
     * @param connectTimeoutMillis 连接超时时间
     * @param soBacklog TCP连接请求队列最大长度
     * @param writeSpinCount 写操作自旋计数
     * @param writeBufferHighWaterMark 写缓冲区高水位标记
     * @param writeBufferLowWaterMark 写缓冲区低水位标记
     * @param soRcvbuf 接收缓冲区大小
     * @param soSndbuf 发送缓冲区大小
     * @param tcpNodelay 是否启用TCP无延迟
     * @param soKeepalive 是否启用TCP保持连接
     * @param soLinger Socket linger时间
     * @param allowHalfClosure 是否允许半关闭
     * @param readerIdleTimeSeconds 读空闲超时时间
     * @param writerIdleTimeSeconds 写空闲超时时间
     * @param allIdleTimeSeconds 所有操作空闲超时时间
     * @param maxFramePayloadLength 最大帧载荷长度
     * @param useEventExecutorGroup 是否使用事件执行器组
     * @param eventExecutorGroupThreads 事件执行器组线程数
     * @param keyPassword SSL密钥密码
     * @param keyStore SSL密钥库路径
     * @param keyStorePassword SSL密钥库密码
     * @param keyStoreType SSL密钥库类型
     * @param trustStore SSL信任库路径
     * @param trustStorePassword SSL信任库密码
     * @param trustStoreType SSL信任库类型
     * @param corsOrigins CORS跨域允许的源站
     * @param corsAllowCredentials CORS是否允许凭证
     */
    public ServerEndpointConfig(String host, int port, int bossLoopGroupThreads, int workerLoopGroupThreads, boolean useCompressionHandler, int connectTimeoutMillis, int soBacklog, int writeSpinCount, int writeBufferHighWaterMark, int writeBufferLowWaterMark, int soRcvbuf, int soSndbuf, boolean tcpNodelay, boolean soKeepalive, int soLinger, boolean allowHalfClosure, int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, int maxFramePayloadLength, boolean useEventExecutorGroup, int eventExecutorGroupThreads, String keyPassword, String keyStore, String keyStorePassword, String keyStoreType, String trustStore, String trustStorePassword, String trustStoreType, String[] corsOrigins, Boolean corsAllowCredentials) {
        if (StringUtils.isEmpty(host) || "0.0.0.0".equals(host) || "0.0.0.0/0.0.0.0".equals(host)) {
            this.HOST = "0.0.0.0";
        } else {
            this.HOST = host;
        }
        this.PORT = getAvailablePort(port);
        this.BOSS_LOOP_GROUP_THREADS = bossLoopGroupThreads;
        this.WORKER_LOOP_GROUP_THREADS = workerLoopGroupThreads;
        this.USE_COMPRESSION_HANDLER = useCompressionHandler;
        this.CONNECT_TIMEOUT_MILLIS = connectTimeoutMillis;
        this.SO_BACKLOG = soBacklog;
        this.WRITE_SPIN_COUNT = writeSpinCount;
        this.WRITE_BUFFER_HIGH_WATER_MARK = writeBufferHighWaterMark;
        this.WRITE_BUFFER_LOW_WATER_MARK = writeBufferLowWaterMark;
        this.SO_RCVBUF = soRcvbuf;
        this.SO_SNDBUF = soSndbuf;
        this.TCP_NODELAY = tcpNodelay;
        this.SO_KEEPALIVE = soKeepalive;
        this.SO_LINGER = soLinger;
        this.ALLOW_HALF_CLOSURE = allowHalfClosure;
        this.READER_IDLE_TIME_SECONDS = readerIdleTimeSeconds;
        this.WRITER_IDLE_TIME_SECONDS = writerIdleTimeSeconds;
        this.ALL_IDLE_TIME_SECONDS = allIdleTimeSeconds;
        this.MAX_FRAME_PAYLOAD_LENGTH = maxFramePayloadLength;
        this.USE_EVENT_EXECUTOR_GROUP = useEventExecutorGroup;
        this.EVENT_EXECUTOR_GROUP_THREADS = eventExecutorGroupThreads;

        this.KEY_PASSWORD = keyPassword;
        this.KEY_STORE = keyStore;
        this.KEY_STORE_PASSWORD = keyStorePassword;
        this.KEY_STORE_TYPE = keyStoreType;
        this.TRUST_STORE = trustStore;
        this.TRUST_STORE_PASSWORD = trustStorePassword;
        this.TRUST_STORE_TYPE = trustStoreType;

        this.CORS_ORIGINS = corsOrigins;
        this.CORS_ALLOW_CREDENTIALS = corsAllowCredentials;
    }

    /**
     * 获取可用端口号
     *
     * 如果指定端口不为0则直接返回，否则获取一个随机可用端口。
     *
     * @param port 指定端口号
     * @return 可用端口号
     */
    private int getAvailablePort(int port) {
        if (port != 0) {
            return port;
        }
        if (randomPort != null && randomPort != 0) {
            return randomPort;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(0);
        Socket socket = new Socket();
        try {
            socket.bind(inetSocketAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int localPort = socket.getLocalPort();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        randomPort = localPort;
        return localPort;
    }

    /**
     * 获取服务器主机地址
     *
     * @return 服务器主机地址
     */
    public String getHost() {
        return HOST;
    }

    /**
     * 获取服务器端口号
     *
     * @return 服务器端口号
     */
    public int getPort() {
        return PORT;
    }

    /**
     * 获取Boss线程组线程数
     *
     * @return Boss线程组线程数
     */
    public int getBossLoopGroupThreads() {
        return BOSS_LOOP_GROUP_THREADS;
    }

    /**
     * 获取Worker线程组线程数
     *
     * @return Worker线程组线程数
     */
    public int getWorkerLoopGroupThreads() {
        return WORKER_LOOP_GROUP_THREADS;
    }

    /**
     * 是否使用压缩处理器
     *
     * @return 是否使用压缩处理器
     */
    public boolean isUseCompressionHandler() {
        return USE_COMPRESSION_HANDLER;
    }

    /**
     * 获取连接超时时间(毫秒)
     *
     * @return 连接超时时间
     */
    public int getConnectTimeoutMillis() {
        return CONNECT_TIMEOUT_MILLIS;
    }

    /**
     * 获取TCP连接请求队列最大长度
     *
     * @return TCP连接请求队列最大长度
     */
    public int getSoBacklog() {
        return SO_BACKLOG;
    }

    /**
     * 获取写操作自旋计数
     *
     * @return 写操作自旋计数
     */
    public int getWriteSpinCount() {
        return WRITE_SPIN_COUNT;
    }

    /**
     * 获取写缓冲区高水位标记
     *
     * @return 写缓冲区高水位标记
     */
    public int getWriteBufferHighWaterMark() {
        return WRITE_BUFFER_HIGH_WATER_MARK;
    }

    /**
     * 获取写缓冲区低水位标记
     *
     * @return 写缓冲区低水位标记
     */
    public int getWriteBufferLowWaterMark() {
        return WRITE_BUFFER_LOW_WATER_MARK;
    }

    /**
     * 获取接收缓冲区大小
     *
     * @return 接收缓冲区大小
     */
    public int getSoRcvbuf() {
        return SO_RCVBUF;
    }

    /**
     * 获取发送缓冲区大小
     *
     * @return 发送缓冲区大小
     */
    public int getSoSndbuf() {
        return SO_SNDBUF;
    }

    /**
     * 是否启用TCP无延迟
     *
     * @return 是否启用TCP无延迟
     */
    public boolean isTcpNodelay() {
        return TCP_NODELAY;
    }

    /**
     * 是否启用TCP保持连接
     *
     * @return 是否启用TCP保持连接
     */
    public boolean isSoKeepalive() {
        return SO_KEEPALIVE;
    }

    /**
     * 获取Socket linger时间
     *
     * @return Socket linger时间
     */
    public int getSoLinger() {
        return SO_LINGER;
    }

    /**
     * 是否允许半关闭
     *
     * @return 是否允许半关闭
     */
    public boolean isAllowHalfClosure() {
        return ALLOW_HALF_CLOSURE;
    }

    /**
     * 获取随机端口号
     *
     * @return 随机端口号
     */
    public static Integer getRandomPort() {
        return randomPort;
    }

    /**
     * 获取读空闲超时时间(秒)
     *
     * @return 读空闲超时时间
     */
    public int getReaderIdleTimeSeconds() {
        return READER_IDLE_TIME_SECONDS;
    }

    /**
     * 获取写空闲超时时间(秒)
     *
     * @return 写空闲超时时间
     */
    public int getWriterIdleTimeSeconds() {
        return WRITER_IDLE_TIME_SECONDS;
    }

    /**
     * 获取所有操作空闲超时时间(秒)
     *
     * @return 所有操作空闲超时时间
     */
    public int getAllIdleTimeSeconds() {
        return ALL_IDLE_TIME_SECONDS;
    }

    /**
     * 获取最大帧载荷长度
     *
     * @return 最大帧载荷长度
     */
    public int getmaxFramePayloadLength() {
        return MAX_FRAME_PAYLOAD_LENGTH;
    }

    /**
     * 是否使用事件执行器组
     *
     * @return 是否使用事件执行器组
     */
    public boolean isUseEventExecutorGroup() {
        return USE_EVENT_EXECUTOR_GROUP;
    }

    /**
     * 获取事件执行器组线程数
     *
     * @return 事件执行器组线程数
     */
    public int getEventExecutorGroupThreads() {
        return EVENT_EXECUTOR_GROUP_THREADS;
    }

    /**
     * 获取SSL密钥密码
     *
     * @return SSL密钥密码
     */
    public String getKeyPassword() {
        return KEY_PASSWORD;
    }

    /**
     * 获取SSL密钥库路径
     *
     * @return SSL密钥库路径
     */
    public String getKeyStore() {
        return KEY_STORE;
    }

    /**
     * 获取SSL密钥库密码
     *
     * @return SSL密钥库密码
     */
    public String getKeyStorePassword() {
        return KEY_STORE_PASSWORD;
    }

    /**
     * 获取SSL密钥库类型
     *
     * @return SSL密钥库类型
     */
    public String getKeyStoreType() {
        return KEY_STORE_TYPE;
    }

    /**
     * 获取SSL信任库路径
     *
     * @return SSL信任库路径
     */
    public String getTrustStore() {
        return TRUST_STORE;
    }

    /**
     * 获取SSL信任库密码
     *
     * @return SSL信任库密码
     */
    public String getTrustStorePassword() {
        return TRUST_STORE_PASSWORD;
    }

    /**
     * 获取SSL信任库类型
     *
     * @return SSL信任库类型
     */
    public String getTrustStoreType() {
        return TRUST_STORE_TYPE;
    }

    /**
     * 获取CORS跨域允许的源站
     *
     * @return CORS跨域允许的源站
     */
    public String[] getCorsOrigins() {
        return CORS_ORIGINS;
    }

    /**
     * 获取CORS是否允许凭证
     *
     * @return CORS是否允许凭证
     */
    public Boolean getCorsAllowCredentials() {
        return CORS_ALLOW_CREDENTIALS;
    }
}
