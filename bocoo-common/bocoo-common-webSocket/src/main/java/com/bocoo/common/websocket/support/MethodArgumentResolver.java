package com.bocoo.common.websocket.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

/**
 * 方法参数解析器接口
 *
 * 该接口定义了WebSocket方法参数解析的标准，
 * 用于将Netty底层对象转换为用户定义方法中的参数类型。
 *
 * @author bocoo
 * @since 1.0.0
 */
public interface MethodArgumentResolver {

	/**
	 * 判断给定的方法参数是否被此解析器支持
	 *
	 * 该方法用于检查当前解析器是否能够处理指定的方法参数，
	 * 通常根据参数类型、注解等条件进行判断。
	 *
	 * @param parameter 需要检查的方法参数
	 * @return 如果此解析器支持该参数则返回true，否则返回false
	 */
	boolean supportsParameter(MethodParameter parameter);

	/**
	 * 解析方法参数的值
	 *
	 * 将Netty底层对象转换为方法参数所需的类型和值。
	 *
	 * @param parameter 方法参数
	 * @param channel Netty通道对象
	 * @param object 需要转换的底层对象（如HttpRequest、WebSocketFrame等）
	 * @return 解析后的参数值，可能为null
	 * @throws Exception 解析过程中可能抛出的异常
	 */
	@Nullable
	Object resolveArgument(MethodParameter parameter, Channel channel,Object object) throws Exception;

}
