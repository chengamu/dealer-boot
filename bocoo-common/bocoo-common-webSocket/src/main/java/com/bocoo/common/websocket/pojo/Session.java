package com.bocoo.common.websocket.pojo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * WebSocket会话封装类
 *
 * 该类封装了Netty的Channel对象，提供了WebSocket会话的高级操作接口。
 * 通过该类可以方便地发送文本消息、二进制消息，管理会话属性，以及获取底层Channel的各种信息。
 *
 * @author Yeauty
 * @version 1.0
 */
public class Session {

    /**
     * 底层Netty通道对象
     */
    private final Channel channel;

    /**
     * 构造函数，使用指定的Channel创建会话
     *
     * @param channel Netty通道对象
     */
    Session(Channel channel) {
        this.channel = channel;
    }

    /**
     * 在握手前设置子协议
     *
     * 该方法应在@BeforeHandshake注解的方法中调用，用于设置WebSocket子协议。
     *
     * @param subprotocols 子协议字符串
     */
    public void setSubprotocols(String subprotocols) {
        setAttribute("subprotocols",subprotocols);
    }

    /**
     * 发送文本消息
     *
     * 将指定的字符串消息作为文本帧发送给客户端。
     *
     * @param message 文本消息内容
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendText(String message) {
        return channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * 发送文本消息
     *
     * 将指定的ByteBuf作为文本帧发送给客户端。
     *
     * @param byteBuf 包含文本内容的ByteBuf
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendText(ByteBuf byteBuf) {
        return channel.writeAndFlush(new TextWebSocketFrame(byteBuf));
    }

    /**
     * 发送文本消息
     *
     * 将指定的ByteBuffer作为文本帧发送给客户端。
     *
     * @param byteBuffer 包含文本内容的ByteBuffer
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendText(ByteBuffer byteBuffer) {
        ByteBuf buffer = channel.alloc().buffer(byteBuffer.remaining());
        buffer.writeBytes(byteBuffer);
        return channel.writeAndFlush(new TextWebSocketFrame(buffer));
    }

    /**
     * 发送文本WebSocket帧
     *
     * 直接发送已构建的TextWebSocketFrame对象。
     *
     * @param textWebSocketFrame 文本WebSocket帧
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendText(TextWebSocketFrame textWebSocketFrame) {
        return channel.writeAndFlush(textWebSocketFrame);
    }

    /**
     * 发送二进制消息
     *
     * 将指定的字节数组作为二进制帧发送给客户端。
     *
     * @param bytes 二进制数据字节数组
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendBinary(byte[] bytes) {
        ByteBuf buffer = channel.alloc().buffer(bytes.length);
        return channel.writeAndFlush(new BinaryWebSocketFrame(buffer.writeBytes(bytes)));
    }

    /**
     * 发送二进制消息
     *
     * 将指定的ByteBuf作为二进制帧发送给客户端。
     *
     * @param byteBuf 包含二进制数据的ByteBuf
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendBinary(ByteBuf byteBuf) {
        return channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }

    /**
     * 发送二进制消息
     *
     * 将指定的ByteBuffer作为二进制帧发送给客户端。
     *
     * @param byteBuffer 包含二进制数据的ByteBuffer
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendBinary(ByteBuffer byteBuffer) {
        ByteBuf buffer = channel.alloc().buffer(byteBuffer.remaining());
        buffer.writeBytes(byteBuffer);
        return channel.writeAndFlush(new BinaryWebSocketFrame(buffer));
    }

    /**
     * 发送二进制WebSocket帧
     *
     * 直接发送已构建的BinaryWebSocketFrame对象。
     *
     * @param binaryWebSocketFrame 二进制WebSocket帧
     * @return ChannelFuture 用于监听发送操作完成状态
     */
    public ChannelFuture sendBinary(BinaryWebSocketFrame binaryWebSocketFrame) {
        return channel.writeAndFlush(binaryWebSocketFrame);
    }

    /**
     * 设置会话属性
     *
     * 在当前会话中存储指定名称和值的属性，可用于在不同方法间传递数据。
     *
     * @param name 属性名称
     * @param value 属性值
     * @param <T> 属性值类型
     */
    public <T> void setAttribute(String name, T value) {
        AttributeKey<T> sessionIdKey = AttributeKey.valueOf(name);
        channel.attr(sessionIdKey).set(value);
    }

    /**
     * 获取会话属性
     *
     * 从当前会话中获取指定名称的属性值。
     *
     * @param name 属性名称
     * @param <T> 属性值类型
     * @return 属性值，如果不存在则返回null
     */
    public <T> T getAttribute(String name) {
        AttributeKey<T> sessionIdKey = AttributeKey.valueOf(name);
        return channel.attr(sessionIdKey).get();
    }

    /**
     * 获取底层Channel对象
     *
     * @return 底层Netty Channel对象
     */
    public Channel channel() {
        return channel;
    }

    /**
     * 返回此Channel的全局唯一标识符
     *
     * @return ChannelId Channel的唯一标识符
     */
    public ChannelId id() {
        return channel.id();
    }

    /**
     * 返回此通道的配置
     *
     * @return ChannelConfig 通道配置对象
     */
    public ChannelConfig config() {
        return channel.config();
    }

    /**
     * 判断Channel是否打开且可能稍后变为活跃状态
     *
     * @return boolean 如果Channel打开则返回true
     */
    public boolean isOpen() {
        return channel.isOpen();
    }

    /**
     * 判断Channel是否已注册到EventLoop
     *
     * @return boolean 如果Channel已注册则返回true
     */
    public boolean isRegistered() {
        return channel.isRegistered();
    }

    /**
     * 判断Channel是否活跃（已连接）
     *
     * @return boolean 如果Channel活跃则返回true
     */
    public boolean isActive() {
        return channel.isActive();
    }

    /**
     * 返回Channel的元数据信息，描述Channel的性质
     *
     * @return ChannelMetadata Channel元数据
     */
    public ChannelMetadata metadata() {
        return channel.metadata();
    }

    /**
     * 返回此通道绑定的本地地址
     *
     * 返回的SocketAddress应被转换为更具体的类型（如InetSocketAddress）以获取详细信息。
     * 如果此通道未绑定则返回null。
     *
     * @return SocketAddress 本地地址，未绑定时返回null
     */
    public SocketAddress localAddress() {
        return channel.localAddress();
    }

    /**
     * 返回此通道连接的远程地址
     *
     * 返回的SocketAddress应被转换为更具体的类型（如InetSocketAddress）以获取详细信息。
     * 如果此通道未连接则返回null。
     *
     * @return SocketAddress 远程地址，未连接时返回null
     */
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    /**
     * 返回当此通道关闭时将被通知的ChannelFuture
     *
     * 此方法始终返回相同的future实例。
     *
     * @return ChannelFuture 通道关闭future
     */
    public ChannelFuture closeFuture() {
        return channel.closeFuture();
    }

    /**
     * 判断I/O线程是否会立即执行请求的写操作
     *
     * 当此方法返回false时，任何写请求都会被排队直到I/O线程准备好处理队列中的写请求。
     *
     * @return boolean 如果可立即写入则返回true
     */
    public boolean isWritable() {
        return channel.isWritable();
    }

    /**
     * 获取在isWritable()返回false之前可以写入的字节数
     *
     * 此数量始终为非负数。如果isWritable()为false则返回0。
     *
     * @return long 可写入的字节数
     */
    public long bytesBeforeUnwritable() {
        return channel.bytesBeforeUnwritable();
    }

    /**
     * 获取在isWritable()返回true之前必须从底层缓冲区排出的字节数
     *
     * 此数量始终为非负数。如果isWritable()为true则返回0。
     *
     * @return long 需要排出的字节数
     */
    public long bytesBeforeWritable() {
        return channel.bytesBeforeWritable();
    }

    /**
     * 返回仅限内部使用的对象，提供不安全操作
     *
     * @return Channel.Unsafe 不安全操作对象
     */
    public Channel.Unsafe unsafe() {
        return channel.unsafe();
    }

    /**
     * 返回分配的ChannelPipeline
     *
     * @return ChannelPipeline 通道流水线
     */
    public ChannelPipeline pipeline() {
        return channel.pipeline();
    }

    /**
     * 返回分配的ByteBufAllocator，用于分配ByteBuf
     *
     * @return ByteBufAllocator 字节缓冲区分配器
     */
    public ByteBufAllocator alloc() {
        return channel.alloc();
    }

    /**
     * 请求从通道读取数据
     *
     * @return Channel 通道对象
     */
    public Channel read() {
        return channel.read();
    }

    /**
     * 刷新通道中的待发送数据
     *
     * @return Channel 通道对象
     */
    public Channel flush() {
        return channel.flush();
    }

    /**
     * 关闭通道
     *
     * @return ChannelFuture 通道关闭future
     */
    public ChannelFuture close() {
        return channel.close();
    }

    /**
     * 使用指定的ChannelPromise关闭通道
     *
     * 在关闭前会发送正常的WebSocket关闭帧。
     *
     * @param promise ChannelPromise对象
     * @return ChannelFuture 通道关闭future
     */
    public ChannelFuture close(ChannelPromise promise) {
        channel.writeAndFlush(new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE));
        return channel.close(promise);
    }

}
