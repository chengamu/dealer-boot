package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.core.MethodParameter;

/**
 * HTTP头部方法参数解析器
 *
 * 该类用于解析WebSocket方法中的HttpHeaders参数，
 * 从HTTP请求中提取头部信息并传递给处理方法。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class HttpHeadersMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持参数类型为HttpHeaders或其子类的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return HttpHeaders.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析方法参数值
     *
     * 从FullHttpRequest对象中提取HTTP头部信息。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object FullHttpRequest对象
     * @return HTTP头部信息
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        return ((FullHttpRequest) object).headers();
    }
}
