package com.bocoo.common.websocket.pojo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.TypeMismatchException;
import com.bocoo.common.websocket.standard.ServerEndpointConfig;
import com.bocoo.common.websocket.support.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * WebSocket端点服务器处理类
 *
 * 该类负责处理WebSocket生命周期中的各种事件，包括连接建立、消息处理、连接关闭等。
 * 它将Netty的底层事件转换为用户定义的POJO方法调用，实现了WebSocket端点的业务逻辑处理。
 *
 * @author Yeauty
 * @version 1.0
 */
public class PojoEndpointServer {

    /**
     * 用于存储WebSocket实现类实例的通道属性键
     */
    private static final AttributeKey<Object> POJO_KEY = AttributeKey.valueOf("WEBSOCKET_IMPLEMENT");

    /**
     * 用于存储WebSocket会话对象的通道属性键
     */
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("WEBSOCKET_SESSION");

    /**
     * 用于存储WebSocket路径的通道属性键
     */
    private static final AttributeKey<String> PATH_KEY = AttributeKey.valueOf("WEBSOCKET_PATH");

    /**
     * 用于存储URI模板变量的通道属性键
     */
    public static final AttributeKey<Map<String, String>> URI_TEMPLATE = AttributeKey.valueOf("WEBSOCKET_URI_TEMPLATE");

    /**
     * 用于存储请求参数的通道属性键
     */
    public static final AttributeKey<Map<String, List<String>>> REQUEST_PARAM = AttributeKey.valueOf("WEBSOCKET_REQUEST_PARAM");

    /**
     * 存储路径与方法映射关系的Map
     */
    private final Map<String, PojoMethodMapping> pathMethodMappingMap = new HashMap<>();

    /**
     * WebSocket服务器端点配置
     */
    private final ServerEndpointConfig config;

    /**
     * WebSocket路径匹配器集合
     */
    private Set<WsPathMatcher> pathMatchers = new HashSet<>();

    /**
     * 内部日志记录器
     */
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PojoEndpointServer.class);

    /**
     * 构造函数，初始化POJO端点服务器
     *
     * @param methodMapping 方法映射对象
     * @param config 服务器端点配置
     * @param path WebSocket路径
     */
    public PojoEndpointServer(PojoMethodMapping methodMapping, ServerEndpointConfig config, String path) {
        addPathPojoMethodMapping(path, methodMapping);
        this.config = config;
    }

    /**
     * 检查指定路径是否有握手前处理方法
     *
     * @param channel Netty通道
     * @param path WebSocket路径
     * @return 是否有握手前处理方法
     */
    public boolean hasBeforeHandshake(Channel channel, String path) {
        PojoMethodMapping methodMapping = getPojoMethodMapping(path, channel);
        return methodMapping.getBeforeHandshake()!=null;
    }

    /**
     * 执行握手前处理逻辑
     *
     * @param channel Netty通道
     * @param req 完整的HTTP请求
     * @param path WebSocket路径
     */
    public void doBeforeHandshake(Channel channel, FullHttpRequest req, String path) {
        PojoMethodMapping methodMapping = null;
        methodMapping = getPojoMethodMapping(path, channel);

        Object implement = null;
        try {
            implement = methodMapping.getEndpointInstance();
        } catch (Exception e) {
            logger.error(e);
            return;
        }
        channel.attr(POJO_KEY).set(implement);
        Session session = new Session(channel);
        channel.attr(SESSION_KEY).set(session);
        Method beforeHandshake = methodMapping.getBeforeHandshake();
        if (beforeHandshake != null) {
            try {
                beforeHandshake.invoke(implement, methodMapping.getBeforeHandshakeArgs(channel, req));
            } catch (TypeMismatchException e) {
                throw e;
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 执行连接打开处理逻辑
     *
     * @param channel Netty通道
     * @param req 完整的HTTP请求
     * @param path WebSocket路径
     */
    public void doOnOpen(Channel channel, FullHttpRequest req, String path) {
        PojoMethodMapping methodMapping = getPojoMethodMapping(path, channel);

        Object implement = channel.attr(POJO_KEY).get();
        if (implement==null){
            try {
                implement = methodMapping.getEndpointInstance();
                channel.attr(POJO_KEY).set(implement);
            } catch (Exception e) {
                logger.error(e);
                return;
            }
            Session session = new Session(channel);
            channel.attr(SESSION_KEY).set(session);
        }

        Method onOpenMethod = methodMapping.getOnOpen();
        if (onOpenMethod != null) {
            try {
                onOpenMethod.invoke(implement, methodMapping.getOnOpenArgs(channel, req));
            } catch (TypeMismatchException e) {
                throw e;
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 执行连接关闭处理逻辑
     *
     * @param channel Netty通道
     */
    public void doOnClose(Channel channel) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                return;
            }
        }
        if (methodMapping.getOnClose() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }
            Object implement = channel.attr(POJO_KEY).get();
            try {
                methodMapping.getOnClose().invoke(implement,
                        methodMapping.getOnCloseArgs(channel));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 执行错误处理逻辑
     *
     * @param channel Netty通道
     * @param throwable 异常对象
     */
    public void doOnError(Channel channel, Throwable throwable) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnError() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }
            Object implement = channel.attr(POJO_KEY).get();
            try {
                Method method = methodMapping.getOnError();
                Object[] args = methodMapping.getOnErrorArgs(channel, throwable);
                method.invoke(implement, args);
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 执行文本消息处理逻辑
     *
     * @param channel Netty通道
     * @param frame WebSocket帧
     */
    public void doOnMessage(Channel channel, WebSocketFrame frame) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnMessage() != null) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            Object implement = channel.attr(POJO_KEY).get();
            try {
                methodMapping.getOnMessage().invoke(implement, methodMapping.getOnMessageArgs(channel, textFrame));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 执行二进制消息处理逻辑
     *
     * @param channel Netty通道
     * @param frame WebSocket帧
     */
    public void doOnBinary(Channel channel, WebSocketFrame frame) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnBinary() != null) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
            Object implement = channel.attr(POJO_KEY).get();
            try {
                methodMapping.getOnBinary().invoke(implement, methodMapping.getOnBinaryArgs(channel, binaryWebSocketFrame));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 执行事件处理逻辑
     *
     * @param channel Netty通道
     * @param evt 事件对象
     */
    public void doOnEvent(Channel channel, Object evt) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            String path = attrPath.get();
            methodMapping = pathMethodMappingMap.get(path);
        }
        if (methodMapping.getOnEvent() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }
            Object implement = channel.attr(POJO_KEY).get();
            try {
                methodMapping.getOnEvent().invoke(implement, methodMapping.getOnEventArgs(channel, evt));
            } catch (Throwable t) {
                logger.error(t);
            }
        }
    }

    /**
     * 获取服务器主机地址
     *
     * @return 主机地址
     */
    public String getHost() {
        return config.getHost();
    }

    /**
     * 获取服务器端口号
     *
     * @return 端口号
     */
    public int getPort() {
        return config.getPort();
    }

    /**
     * 获取路径匹配器集合
     *
     * @return 路径匹配器集合
     */
    public Set<WsPathMatcher> getPathMatcherSet() {
        return pathMatchers;
    }

    /**
     * 添加路径与方法映射关系
     *
     * @param path WebSocket路径
     * @param pojoMethodMapping POJO方法映射对象
     */
    public void addPathPojoMethodMapping(String path, PojoMethodMapping pojoMethodMapping) {
        pathMethodMappingMap.put(path, pojoMethodMapping);
        for (MethodArgumentResolver onOpenArgResolver : pojoMethodMapping.getOnOpenArgResolvers()) {
            if (onOpenArgResolver instanceof PathVariableMethodArgumentResolver || onOpenArgResolver instanceof PathVariableMapMethodArgumentResolver) {
                pathMatchers.add(new AntPathMatcherWrapper(path));
                return;
            }
        }
        pathMatchers.add(new DefaultPathMatcher(path));
    }

    /**
     * 获取指定路径的POJO方法映射
     *
     * @param path WebSocket路径
     * @param channel Netty通道
     * @return POJO方法映射对象
     */
    private PojoMethodMapping getPojoMethodMapping(String path, Channel channel) {
        PojoMethodMapping methodMapping;
        if (pathMethodMappingMap.size() == 1) {
            methodMapping = pathMethodMappingMap.values().iterator().next();
        } else {
            Attribute<String> attrPath = channel.attr(PATH_KEY);
            attrPath.set(path);
            methodMapping = pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                throw new RuntimeException("path " + path + " is not in pathMethodMappingMap ");
            }
        }
        return methodMapping;
    }
}
