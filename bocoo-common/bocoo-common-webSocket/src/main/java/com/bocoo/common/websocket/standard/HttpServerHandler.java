package com.bocoo.common.websocket.standard;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;
import com.bocoo.common.websocket.pojo.PojoEndpointServer;
import com.bocoo.common.websocket.support.WsPathMatcher;

import java.io.InputStream;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP服务器请求处理器
 *
 * 该类负责处理WebSocket握手前的HTTP请求，包括路径匹配、协议验证、握手处理等。
 * 它是Netty WebSocket服务器与客户端建立连接的关键环节。
 *
 * @author bocoo
 * @since 1.0.0
 */
class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * POJO端点服务器实例
     */
    private final PojoEndpointServer pojoEndpointServer;

    /**
     * 服务器端点配置
     */
    private final ServerEndpointConfig config;

    /**
     * 事件执行器组
     */
    private final EventExecutorGroup eventExecutorGroup;

    /**
     * 是否启用CORS
     */
    private final boolean isCors;

    /**
     * favicon图标字节缓冲区
     */
    private static ByteBuf faviconByteBuf = null;

    /**
     * 404错误页面字节缓冲区
     */
    private static ByteBuf notFoundByteBuf = null;

    /**
     * 400错误页面字节缓冲区
     */
    private static ByteBuf badRequestByteBuf = null;

    /**
     * 403错误页面字节缓冲区
     */
    private static ByteBuf forbiddenByteBuf = null;

    /**
     * 500错误页面字节缓冲区
     */
    private static ByteBuf internalServerErrorByteBuf = null;

    /**
     * 静态初始化块，加载静态资源
     */
    static {
        faviconByteBuf = buildStaticRes("/favicon.ico");
        notFoundByteBuf = buildStaticRes("/public/error/404.html");
        badRequestByteBuf = buildStaticRes("/public/error/400.html");
        forbiddenByteBuf = buildStaticRes("/public/error/403.html");
        internalServerErrorByteBuf = buildStaticRes("/public/error/500.html");
        if (notFoundByteBuf == null) {
            notFoundByteBuf = buildStaticRes("/public/error/4xx.html");
        }
        if (badRequestByteBuf == null) {
            badRequestByteBuf = buildStaticRes("/public/error/4xx.html");
        }
        if (forbiddenByteBuf == null) {
            forbiddenByteBuf = buildStaticRes("/public/error/4xx.html");
        }
        if (internalServerErrorByteBuf == null) {
            internalServerErrorByteBuf = buildStaticRes("/public/error/5xx.html");
        }
    }

    /**
     * 构建静态资源字节缓冲区
     *
     * 从classpath中读取指定路径的静态资源文件，转换为ByteBuf对象。
     *
     * @param resPath 资源路径
     * @return ByteBuf 字节缓冲区对象，如果资源不存在则返回null
     */
    private static ByteBuf buildStaticRes(String resPath) {
        try {
            InputStream inputStream = HttpServerHandler.class.getResourceAsStream(resPath);
            if (inputStream != null) {
                int available = inputStream.available();
                if (available != 0) {
                    byte[] bytes = new byte[available];
                    inputStream.read(bytes);
                    return ByteBufAllocator.DEFAULT.buffer(bytes.length).writeBytes(bytes);
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 构造函数，初始化HTTP服务器处理器
     *
     * @param pojoEndpointServer POJO端点服务器
     * @param config 服务器端点配置
     * @param eventExecutorGroup 事件执行器组
     * @param isCors 是否启用CORS
     */
    public HttpServerHandler(PojoEndpointServer pojoEndpointServer, ServerEndpointConfig config, EventExecutorGroup eventExecutorGroup, boolean isCors) {
        this.pojoEndpointServer = pojoEndpointServer;
        this.config = config;
        this.eventExecutorGroup = eventExecutorGroup;
        this.isCors = isCors;
    }

    /**
     * 处理入站的完整HTTP请求
     *
     * @param ctx Channel处理器上下文
     * @param msg 完整HTTP请求
     * @throws Exception 异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        try {
            handleHttpRequest(ctx, msg);
        } catch (TypeMismatchException e) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        } catch (Exception e) {
            FullHttpResponse res;
            if (internalServerErrorByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, internalServerErrorByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            }
            sendHttpResponse(ctx, msg, res);
            e.printStackTrace();
        }
    }

    /**
     * 异常捕获处理方法
     *
     * 当通道中发生异常时，调用POJO端点服务器的错误处理方法。
     *
     * @param ctx Channel处理器上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        pojoEndpointServer.doOnError(ctx.channel(), cause);
    }

    /**
     * 通道非活跃状态处理方法
     *
     * 当通道变为非活跃状态时，调用POJO端点服务器的关闭处理方法。
     *
     * @param ctx Channel处理器上下文
     * @throws Exception 异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx.channel());
        super.channelInactive(ctx);
    }

    /**
     * 处理HTTP请求
     *
     * 执行WebSocket握手前的各种验证和处理，包括：
     * 1. 请求有效性检查
     * 2. HTTP方法验证
     * 3. 主机头验证
     * 4. 路径匹配
     * 5. WebSocket握手协议验证
     * 6. 握手前处理
     * 7. WebSocket握手
     *
     * @param ctx Channel处理器上下文
     * @param req 完整HTTP请求
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        FullHttpResponse res;
        // 处理错误的请求
        if (!req.decoderResult().isSuccess()) {
            if (badRequestByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, badRequestByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        // 仅允许GET方法
        if (req.method() != GET) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        HttpHeaders headers = req.headers();
        String host = headers.get(HttpHeaderNames.HOST);
        // 检查HOST头是否存在
        if (StringUtils.isEmpty(host)) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        // 检查主机地址是否匹配
        if (!StringUtils.isEmpty(pojoEndpointServer.getHost()) && !pojoEndpointServer.getHost().equals("0.0.0.0") && !pojoEndpointServer.getHost().equals(host.split(":")[0])) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        String path = decoder.path();
        // 处理favicon请求
        if ("/favicon.ico".equals(path)) {
            if (faviconByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, OK, faviconByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        Channel channel = ctx.channel();

        // 路径匹配
        String pattern = null;
        Set<WsPathMatcher> pathMatcherSet = pojoEndpointServer.getPathMatcherSet();
        for (WsPathMatcher pathMatcher : pathMatcherSet) {
            if (pathMatcher.matchAndExtract(decoder, channel)) {
                pattern = pathMatcher.getPattern();
                break;
            }
        }

        // 路径未匹配
        if (pattern == null) {
            if (notFoundByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, notFoundByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        // 检查是否为WebSocket握手请求
        if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY) || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            }
            sendHttpResponse(ctx, req, res);
            return;
        }

        String subprotocols = null;

        // 执行握手前处理
        if (pojoEndpointServer.hasBeforeHandshake(channel, pattern)) {
            pojoEndpointServer.doBeforeHandshake(channel, req, pattern);
            if (!channel.isActive()) {
                return;
            }

            AttributeKey<String> subprotocolsAttrKey = AttributeKey.valueOf("subprotocols");
            if (channel.hasAttr(subprotocolsAttrKey)) {
                subprotocols = ctx.channel().attr(subprotocolsAttrKey).get();
            }
        }
        ChannelPipeline pipeline = ctx.pipeline();
        // 如果启用压缩处理器
        if (config.isUseCompressionHandler()) {
            // 添加WebSocket压缩处理器，但不进行握手
            pipeline.addLast(new WebSocketServerCompressionHandler());
            // 让请求通过WebSocket压缩处理器转发到下一个处理器
            ctx.fireChannelRead(req.retain());
        }
        // 执行WebSocket握手
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), subprotocols, true, config.getmaxFramePayloadLength());
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            pipeline.remove(ctx.name());
            // 如果配置了空闲检测时间，则添加空闲状态处理器
            if (config.getReaderIdleTimeSeconds() != 0 || config.getWriterIdleTimeSeconds() != 0 || config.getAllIdleTimeSeconds() != 0) {
                pipeline.addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));
            }
            // 添加WebSocket帧聚合器
            pipeline.addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE));
            // 根据配置决定是否使用事件执行器组
            if (config.isUseEventExecutorGroup()) {
                pipeline.addLast(eventExecutorGroup, new WebSocketServerHandler(pojoEndpointServer));
            } else {
                pipeline.addLast(new WebSocketServerHandler(pojoEndpointServer));
            }
            String finalPattern = pattern;

            String header = headers.get(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL);
            HttpHeaders httpHeaders = null;
            if (header!=null) {
                httpHeaders = new DefaultHttpHeaders().add(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, header);
            }
            // 执行握手并添加监听器
            final ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req,httpHeaders,ctx.channel().newPromise());

            handshakeFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // 如果启用了CORS，则移除CORS处理器
                    if (isCors) {
                        pipeline.remove(CorsHandler.class);
                    }
                    // 调用POJO端点服务器的连接打开处理方法
                    pojoEndpointServer.doOnOpen(channel, req, finalPattern);
                } else {
                    // 握手失败，关闭连接
                    handshaker.close(channel, new CloseWebSocketFrame());
                }
            });
        }

    }

    /**
     * 发送HTTP响应
     *
     * 构建并发送HTTP响应给客户端，根据需要关闭连接。
     *
     * @param ctx Channel处理器上下文
     * @param req 原始HTTP请求
     * @param res HTTP响应
     */
    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // 如果响应状态码不是200且响应内容为空，则生成错误页面
        int statusCode = res.status().code();
        if (statusCode != OK.code() && res.content().readableBytes() == 0) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        HttpUtil.setContentLength(res, res.content().readableBytes());

        // 发送响应并在必要时关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || statusCode != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 获取WebSocket位置
     *
     * 根据HTTP请求构建WebSocket连接地址。
     *
     * @param req 完整HTTP请求
     * @return WebSocket连接地址
     */
    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }
}
