package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.annotation.PathVariable;

import java.util.Map;

import static com.bocoo.common.websocket.pojo.PojoEndpointServer.URI_TEMPLATE;

/**
 * 路径变量方法参数解析器
 *
 * 该类用于解析WebSocket方法中使用@PathVariable注解的单个路径变量参数，
 * 从URI模板中提取指定名称的路径变量并转换为方法参数所需的类型。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class PathVariableMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * Spring抽象Bean工厂
     */
    private AbstractBeanFactory beanFactory;

    /**
     * 构造函数，初始化路径变量方法参数解析器
     *
     * @param beanFactory Spring抽象Bean工厂
     */
    public PathVariableMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持标记了@PathVariable注解的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathVariable.class);
    }

    /**
     * 解析方法参数值
     *
     * 从Channel属性中获取指定名称的URI模板变量，并转换为参数所需类型。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object 请求对象
     * @return 解析后的参数值
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        PathVariable ann = parameter.getParameterAnnotation(PathVariable.class);
        String name = ann.name();
        if (name.isEmpty()) {
            name = parameter.getParameterName();
            if (name == null) {
                throw new IllegalArgumentException(
                        "参数类型 [" + parameter.getNestedParameterType().getName() +
                                "] 的名称不可用，且在类文件中也未找到参数名称信息。");
            }
        }
        Map<String, String> uriTemplateVars = channel.attr(URI_TEMPLATE).get();
        Object arg = (uriTemplateVars != null ? uriTemplateVars.get(name) : null);
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        return typeConverter.convertIfNecessary(arg, parameter.getParameterType());
    }
}
