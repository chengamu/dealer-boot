package com.bocoo.common.websocket.support;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.annotation.OnBinary;

/**
 * 字节数组方法参数解析器
 *
 * 该类用于解析WebSocket二进制消息方法中的byte[]参数，
 * 将BinaryWebSocketFrame中的二进制数据转换为字节数组。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class ByteMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 只支持标记了@OnBinary注解且参数类型为byte[]的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnBinary.class) && byte[].class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析方法参数值
     *
     * 将BinaryWebSocketFrame中的二进制内容读取为字节数组。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object BinaryWebSocketFrame对象
     * @return 字节数组
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) object;
        ByteBuf content = binaryWebSocketFrame.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return bytes;
    }
}
