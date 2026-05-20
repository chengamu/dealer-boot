package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.annotation.OnMessage;

/**
 * 文本方法参数解析器
 *
 * 该类用于解析WebSocket消息处理方法中的String类型参数，
 * 将TextWebSocketFrame中的文本内容提取并注入到方法参数中。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class TextMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持标记了@OnMessage注解且参数类型为String的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnMessage.class) && String.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析方法参数值
     *
     * 从TextWebSocketFrame中提取文本内容。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object TextWebSocketFrame对象
     * @return 文本内容
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        TextWebSocketFrame textFrame = (TextWebSocketFrame) object;
        return textFrame.text();
    }
}
