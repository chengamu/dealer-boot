package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.pojo.Session;

import static com.bocoo.common.websocket.pojo.PojoEndpointServer.SESSION_KEY;

/**
 * 会话方法参数解析器
 *
 * 该类用于解析WebSocket方法中类型为Session的参数，
 * 从Netty通道中获取WebSocket会话对象并注入到方法参数中。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class SessionMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持参数类型为Session或其子类的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Session.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析方法参数值
     *
     * 从Netty通道属性中获取WebSocket会话对象。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object 请求对象
     * @return WebSocket会话对象
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        Session session = channel.attr(SESSION_KEY).get();
        return session;
    }
}
