package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.bocoo.common.websocket.annotation.PathVariable;

import java.util.Collections;
import java.util.Map;

import static com.bocoo.common.websocket.pojo.PojoEndpointServer.URI_TEMPLATE;

/**
 * 路径变量Map方法参数解析器
 *
 * 该类用于解析WebSocket方法中使用@PathVariable注解的Map类型参数，
 * 将URI模板中的所有路径变量以键值对的形式注入到方法参数中。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class PathVariableMapMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持标记了@PathVariable注解且参数类型为Map但未指定具体名称的情况。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        PathVariable ann = parameter.getParameterAnnotation(PathVariable.class);
        return (ann != null && Map.class.isAssignableFrom(parameter.getParameterType()) &&
                !StringUtils.hasText(ann.value()));
    }

    /**
     * 解析方法参数值
     *
     * 从Channel属性中获取URI模板变量，并以Map形式返回。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object 请求对象
     * @return URI模板变量Map
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
        if (!CollectionUtils.isEmpty(uriTemplateVars)) {
            return uriTemplateVars;
        } else {
            return Collections.emptyMap();
        }
    }
}
