package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.annotation.OnEvent;

/**
 * 事件方法参数解析器
 *
 * 该类用于解析WebSocket事件处理方法中的参数，
 * 支持将Netty事件对象转换为方法参数所需的类型。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class EventMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * Spring抽象Bean工厂
     */
    private AbstractBeanFactory beanFactory;

    /**
     * 构造函数，初始化事件方法参数解析器
     *
     * @param beanFactory Spring抽象Bean工厂
     */
    public EventMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 判断是否支持给定的方法参数
     *
     * 只支持标记了@OnEvent注解的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnEvent.class);
    }

    /**
     * 解析方法参数值
     *
     * 将Netty事件对象转换为方法参数所需的类型。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object 事件对象
     * @return 转换后的参数值
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        if (object==null) {
            return null;
        }
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        return typeConverter.convertIfNecessary(object, parameter.getParameterType());
    }
}
