package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * 默认路径匹配器
 *
 * 该类实现WsPathMatcher接口，提供精确路径匹配功能。
 * 用于匹配WebSocket端点的固定路径，不支持路径变量提取。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class DefaultPathMatcher implements WsPathMatcher {

    /**
     * 路径模式
     */
    private String pattern;

    /**
     * 构造函数，初始化默认路径匹配器
     *
     * @param pattern 路径模式
     */
    public DefaultPathMatcher(String pattern) {
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
     * 匹配路径
     *
     * 通过精确匹配的方式判断请求路径是否与配置的路径模式一致。
     *
     * @param decoder 查询字符串解码器，包含请求路径信息
     * @param channel Netty通道对象
     * @return 是否匹配成功
     */
    @Override
    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        if (!pattern.equals(decoder.path())) {
            return false;
        }
        return true;
    }
}
