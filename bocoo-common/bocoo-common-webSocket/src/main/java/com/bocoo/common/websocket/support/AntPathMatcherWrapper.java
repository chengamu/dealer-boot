package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.bocoo.common.websocket.pojo.PojoEndpointServer.URI_TEMPLATE;

/**
 * Ant路径匹配器包装类
 *
 * 该类继承自Spring的AntPathMatcher并实现WsPathMatcher接口，
 * 用于支持RESTful风格的WebSocket路径匹配，可以提取路径中的变量参数。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class AntPathMatcherWrapper extends AntPathMatcher implements WsPathMatcher {

    /**
     * 路径模式
     */
    private String pattern;

    /**
     * 构造函数，初始化Ant路径匹配器包装类
     *
     * @param pattern 路径模式
     */
    public AntPathMatcherWrapper(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 获取路径模式
     *
     * @return 路径模式
     */
    @Override
    public String getPattern() {
        return this.pattern;
    }

    /**
     * 匹配并提取路径变量
     *
     * 使用Ant风格的路径匹配规则匹配给定的路径，并提取路径中的变量参数。
     * 如果匹配成功，将提取到的变量参数存储到Channel的属性中。
     *
     * @param decoder 查询字符串解码器，包含请求路径信息
     * @param channel Netty通道对象
     * @return 是否匹配成功
     */
    @Override
    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        Map<String, String> variables = new LinkedHashMap<>();
        boolean result = doMatch(pattern, decoder.path(), true, variables);
        if (result) {
            channel.attr(URI_TEMPLATE).set(variables);
            return true;
        }
        return false;
    }
}
