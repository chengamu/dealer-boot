package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * WebSocket路径匹配器接口
 *
 * 该接口定义了WebSocket路径匹配的标准，
 * 用于匹配请求路径并提取路径中的变量参数。
 *
 * @author bocoo
 * @since 1.0.0
 */
public interface WsPathMatcher {

    /**
     * 获取路径模式
     *
     * @return 路径模式字符串
     */
    String getPattern();

    /**
     * 匹配路径并提取变量
     *
     * 根据路径模式匹配请求路径，如果匹配成功则提取路径中的变量参数。
     *
     * @param decoder 查询字符串解码器，包含请求路径信息
     * @param channel Netty通道对象
     * @return 是否匹配成功
     */
    boolean matchAndExtract(QueryStringDecoder decoder, Channel channel);
}
