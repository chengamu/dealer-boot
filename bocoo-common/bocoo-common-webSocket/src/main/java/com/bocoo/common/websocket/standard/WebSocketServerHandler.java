package com.bocoo.common.websocket.standard;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import com.bocoo.common.websocket.pojo.PojoEndpointServer;

/**
 * WebSocket服务器消息处理器
 *
 * 该类继承自Netty的SimpleChannelInboundHandler，专门用于处理WebSocket帧消息。
 * 它负责将不同类型的WebSocket帧分发给对应的POJO端点服务器方法进行处理。
 *
 * @author bocoo
 * @since 1.0.0
 */
class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    /**
     * POJO端点服务器实例
     */
    private final PojoEndpointServer pojoEndpointServer;

    /**
     * 构造函数，初始化WebSocket服务器处理器
     *
     * @param pojoEndpointServer POJO端点服务器实例
     */
    public WebSocketServerHandler(PojoEndpointServer pojoEndpointServer) {
        this.pojoEndpointServer = pojoEndpointServer;
    }

    /**
     * 处理入站的WebSocket帧消息
     *
     * 该方法是SimpleChannelInboundHandler接口的回调方法，
     * 当有WebSocket帧消息到达时自动调用。
     *
     * @param ctx Channel处理器上下文
     * @param msg WebSocket帧消息
     * @throws Exception 异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        handleWebSocketFrame(ctx, msg);
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
     * 当通道变为非活跃状态时（连接断开），调用POJO端点服务器的关闭处理方法。
     *
     * @param ctx Channel处理器上下文
     * @throws Exception 异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx.channel());
    }

    /**
     * 用户事件触发处理方法
     *
     * 当有用户事件触发时（如心跳事件），调用POJO端点服务器的事件处理方法。
     *
     * @param ctx Channel处理器上下文
     * @param evt 事件对象
     * @throws Exception 异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        pojoEndpointServer.doOnEvent(ctx.channel(), evt);
    }

    /**
     * 处理WebSocket帧
     *
     * 根据WebSocket帧的类型，分发给对应的处理逻辑：
     * 1. 文本帧：调用消息处理方法
     * 2. Ping帧：回复Pong帧
     * 3. 关闭帧：关闭连接
     * 4. 二进制帧：调用二进制消息处理方法
     * 5. Pong帧：不处理
     *
     * @param ctx Channel处理器上下文
     * @param frame WebSocket帧
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 处理文本WebSocket帧
        if (frame instanceof TextWebSocketFrame) {
            pojoEndpointServer.doOnMessage(ctx.channel(), frame);
            return;
        }
        // 处理Ping帧，回复Pong帧
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 处理关闭帧，关闭连接
        if (frame instanceof CloseWebSocketFrame) {
            ctx.writeAndFlush(frame.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        // 处理二进制WebSocket帧
        if (frame instanceof BinaryWebSocketFrame) {
            pojoEndpointServer.doOnBinary(ctx.channel(), frame);
            return;
        }
        // 处理Pong帧，无需特殊处理
        if (frame instanceof PongWebSocketFrame) {
            return;
        }
    }

}
