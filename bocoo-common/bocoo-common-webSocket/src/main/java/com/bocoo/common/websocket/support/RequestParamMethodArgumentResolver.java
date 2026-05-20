package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;
import com.bocoo.common.websocket.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import static com.bocoo.common.websocket.pojo.PojoEndpointServer.REQUEST_PARAM;

/**
 * 请求参数方法参数解析器
 *
 * 该类用于解析WebSocket方法中使用@RequestParam注解的单个请求参数，
 * 从HTTP请求中提取指定名称的查询参数并转换为方法参数所需的类型。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class RequestParamMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * Spring抽象Bean工厂
     */
    private AbstractBeanFactory beanFactory;

    /**
     * 构造函数，初始化请求参数方法参数解析器
     *
     * @param beanFactory Spring抽象Bean工厂
     */
    public RequestParamMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持标记了@RequestParam注解的方法参数。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    /**
     * 解析方法参数值
     *
     * 从HTTP请求中提取指定名称的查询参数，并根据参数类型进行转换。
     * 支持单值参数和List类型参数，如果参数不存在则使用默认值。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object FullHttpRequest对象
     * @return 解析后的参数值
     * @throws Exception 异常
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        String name = ann.name();
        if (name.isEmpty()) {
            name = parameter.getParameterName();
            if (name == null) {
                throw new IllegalArgumentException(
                        "参数类型 [" + parameter.getNestedParameterType().getName() +
                                "] 的名称不可用，且在类文件中也未找到参数名称信息。");
            }
        }

        if (!channel.hasAttr(REQUEST_PARAM)) {
            QueryStringDecoder decoder = new QueryStringDecoder(((FullHttpRequest) object).uri());
            channel.attr(REQUEST_PARAM).set(decoder.parameters());
        }

        Map<String, List<String>> requestParams = channel.attr(REQUEST_PARAM).get();
        List<String> arg = (requestParams != null ? requestParams.get(name) : null);
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        if (arg == null) {
            if ("\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n".equals(ann.defaultValue())) {
                return null;
            }else {
                return typeConverter.convertIfNecessary(ann.defaultValue(), parameter.getParameterType());
            }
        }
        if (List.class.isAssignableFrom(parameter.getParameterType())) {
            return typeConverter.convertIfNecessary(arg, parameter.getParameterType());
        } else {
            return typeConverter.convertIfNecessary(arg.get(0), parameter.getParameterType());
        }
    }
}
