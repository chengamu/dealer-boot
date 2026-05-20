package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.core.MethodParameter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import com.bocoo.common.websocket.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import static com.bocoo.common.websocket.pojo.PojoEndpointServer.REQUEST_PARAM;

/**
 * 请求参数Map方法参数解析器
 *
 * 该类用于解析WebSocket方法中使用@RequestParam注解的Map类型参数，
 * 将HTTP请求中的查询参数以键值对的形式注入到方法参数中。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class RequestParamMapMethodArgumentResolver implements MethodArgumentResolver {

    /**
     * 判断是否支持给定的方法参数
     *
     * 支持标记了@RequestParam注解且参数类型为Map但未指定具体名称的情况。
     *
     * @param parameter 方法参数
     * @return 是否支持该参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        return (requestParam != null && Map.class.isAssignableFrom(parameter.getParameterType()) &&
                !StringUtils.hasText(requestParam.name()));
    }

    /**
     * 解析方法参数值
     *
     * 从HTTP请求中提取查询参数，并根据参数类型决定返回MultiValueMap还是单值Map。
     *
     * @param parameter 方法参数
     * @param channel Netty通道
     * @param object FullHttpRequest对象
     * @return 请求参数Map
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
        MultiValueMap multiValueMap = new LinkedMultiValueMap(requestParams);
        if (MultiValueMap.class.isAssignableFrom(parameter.getParameterType())) {
            return multiValueMap;
        } else {
            return multiValueMap.toSingleValueMap();
        }
    }
}
