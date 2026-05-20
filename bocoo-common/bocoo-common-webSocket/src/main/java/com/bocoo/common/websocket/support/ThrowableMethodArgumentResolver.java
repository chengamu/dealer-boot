package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.annotation.OnError;

/**
 * 异常方法参数解析器
 *
 * 该类用于解析WebSocket异常处理方法中的Throwable类型参数，
 * 将发生的异常对象注入到标记了@OnError注解的方法参数中。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class ThrowableMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持标记了@OnError注解且参数类型为Throwable或其子类的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnError.class) && Throwable.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析方法参数值
     *
     * 检查对象是否为Throwable类型，如果是则直接返回。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object 异常对象
     * @return Throwable对象或null
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        if (object instanceof Throwable) {
            return object;
        }
        return null;
    }
}
