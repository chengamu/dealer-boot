package com.bocoo.common.websocket.standard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.util.StringUtils;
import com.bocoo.common.websocket.pojo.PojoEndpointServer;
import com.bocoo.common.websocket.util.SslUtils;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * WebSocket服务器实现类
 *
 * 该类基于Netty实现WebSocket服务器，负责创建和配置Netty服务器引导程序，
 * 设置各种网络参数、SSL配置、CORS配置等，并启动WebSocket服务器。
 *
 * @author Yeauty
 * @version 1.0
 */
public class WebsocketServer {

    /**
     * POJO端点服务器实例
     */
    private final PojoEndpointServer pojoEndpointServer;

    /**
     * 服务器端点配置
     */
    private final ServerEndpointConfig config;

    /**
     * 内部日志记录器
     */
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebsocketServer.class);

    /**
     * 构造函数，初始化WebSocket服务器
     *
     * @param webSocketServerHandler POJO端点服务器处理器
     * @param serverEndpointConfig 服务器端点配置
     */
    public WebsocketServer(PojoEndpointServer webSocketServerHandler, ServerEndpointConfig serverEndpointConfig) {
        this.pojoEndpointServer = webSocketServerHandler;
        this.config = serverEndpointConfig;

    }

    /**
     * 初始化并启动WebSocket服务器
     *
     * 配置Netty服务器的各种参数，包括：
     * 1. SSL上下文配置
     * 2. CORS跨域配置
     * 3. 线程池配置
     * 4. 网络参数配置
     * 5. 通道处理器链配置
     * 6. 绑定端口并启动服务器
     *
     * @throws InterruptedException 线程中断异常
     * @throws SSLException SSL异常
     */
    public void init() throws InterruptedException, SSLException {
        EventExecutorGroup eventExecutorGroup = null;
        final SslContext sslCtx;
        // 创建SSL上下文
        if (!StringUtils.isEmpty(config.getKeyStore())) {
            sslCtx = SslUtils.createSslContext(config.getKeyPassword(), config.getKeyStore(), config.getKeyStoreType(), config.getKeyStorePassword(), config.getTrustStore(), config.getTrustStoreType(), config.getTrustStorePassword());
        } else {
            sslCtx = null;
        }
        String[] corsOrigins = config.getCorsOrigins();
        Boolean corsAllowCredentials = config.getCorsAllowCredentials();
        final CorsConfig corsConfig = createCorsConfig(corsOrigins, corsAllowCredentials);

        // 创建事件执行器组
        if (config.isUseEventExecutorGroup()) {
            eventExecutorGroup = new DefaultEventExecutorGroup(config.getEventExecutorGroupThreads() == 0 ? 16 : config.getEventExecutorGroupThreads());
        }
        // 创建Boss和Worker线程组
        EventLoopGroup boss = new NioEventLoopGroup(config.getBossLoopGroupThreads());
        EventLoopGroup worker = new NioEventLoopGroup(config.getWorkerLoopGroupThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventExecutorGroup finalEventExecutorGroup = eventExecutorGroup;
        // 配置服务器引导程序
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                .option(ChannelOption.SO_BACKLOG, config.getSoBacklog())
                .childOption(ChannelOption.WRITE_SPIN_COUNT, config.getWriteSpinCount())
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(config.getWriteBufferLowWaterMark(), config.getWriteBufferHighWaterMark()))
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, config.isSoKeepalive())
                .childOption(ChannelOption.SO_LINGER, config.getSoLinger())
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, config.isAllowHalfClosure())
                .handler(new LoggingHandler(LogLevel.DEBUG))
                // 配置子通道初始化器
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 添加SSL处理器
                        if (sslCtx != null) {
                            pipeline.addFirst(sslCtx.newHandler(ch.alloc()));
                        }
                        // 添加HTTP编解码器
                        pipeline.addLast(new HttpServerCodec());
                        // 添加HTTP对象聚合器
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        // 添加CORS处理器
                        if (corsConfig != null) {
                            pipeline.addLast(new CorsHandler(corsConfig));
                        }
                        // 添加HTTP服务器处理器
                        pipeline.addLast(new HttpServerHandler(pojoEndpointServer, config, finalEventExecutorGroup, corsConfig != null));
                    }
                });

        // 配置接收缓冲区大小
        if (config.getSoRcvbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, config.getSoRcvbuf());
        }

        // 配置发送缓冲区大小
        if (config.getSoSndbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, config.getSoSndbuf());
        }

        ChannelFuture channelFuture;
        // 绑定服务器地址和端口
        if ("0.0.0.0".equals(config.getHost())) {
            channelFuture = bootstrap.bind(config.getPort());
        } else {
            try {
                channelFuture = bootstrap.bind(new InetSocketAddress(InetAddress.getByName(config.getHost()), config.getPort()));
            } catch (UnknownHostException e) {
                channelFuture = bootstrap.bind(config.getHost(), config.getPort());
                e.printStackTrace();
            }
        }

        // 添加监听器处理绑定结果
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
        });

        // 添加JVM关闭钩子，优雅关闭线程组
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }));
    }

    /**
     * 创建CORS配置
     *
     * 根据配置的CORS源站和凭证设置，构建CORS配置对象。
     *
     * @param corsOrigins CORS允许的源站数组
     * @param corsAllowCredentials 是否允许凭证
     * @return CORS配置对象，如果没有配置则返回null
     */
    private CorsConfig createCorsConfig(String[] corsOrigins, Boolean corsAllowCredentials) {
        if (corsOrigins.length == 0) {
            return null;
        }
        CorsConfigBuilder corsConfigBuilder = null;
        // 检查是否允许所有源站
        for (String corsOrigin : corsOrigins) {
            if ("*".equals(corsOrigin)) {
                corsConfigBuilder = CorsConfigBuilder.forAnyOrigin();
                break;
            }
        }
        // 如果没有通配符，则指定具体的源站
        if (corsConfigBuilder == null) {
            corsConfigBuilder = CorsConfigBuilder.forOrigins(corsOrigins);
        }
        // 配置凭证允许
        if (corsAllowCredentials != null && corsAllowCredentials) {
            corsConfigBuilder.allowCredentials();
        }
        corsConfigBuilder.allowNullOrigin();
        return corsConfigBuilder.build();
    }

    /**
     * 获取POJO端点服务器
     *
     * @return POJO端点服务器实例
     */
    public PojoEndpointServer getPojoEndpointServer() {
        return pojoEndpointServer;
    }
}
