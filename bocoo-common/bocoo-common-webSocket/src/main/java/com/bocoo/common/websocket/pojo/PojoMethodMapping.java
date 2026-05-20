package com.bocoo.common.websocket.pojo;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import com.bocoo.common.websocket.annotation.*;
import com.bocoo.common.websocket.exception.DeploymentException;
import com.bocoo.common.websocket.support.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WebSocket POJO方法映射类
 *
 * 该类负责扫描和解析WebSocket端点类中的注解方法，并建立方法与参数解析器之间的映射关系。
 * 它将用户定义的POJO类中的注解方法（如@OnOpen、@OnMessage等）与对应的参数解析器进行绑定，
 * 实现WebSocket事件到具体方法调用的映射。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class PojoMethodMapping {

    /**
     * 参数名称发现器，用于获取方法参数的名称
     */
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 握手前处理方法
     */
    private final Method beforeHandshake;

    /**
     * 连接打开处理方法
     */
    private final Method onOpen;

    /**
     * 连接关闭处理方法
     */
    private final Method onClose;

    /**
     * 异常处理方法
     */
    private final Method onError;

    /**
     * 文本消息处理方法
     */
    private final Method onMessage;

    /**
     * 二进制消息处理方法
     */
    private final Method onBinary;

    /**
     * 事件处理方法
     */
    private final Method onEvent;

    /**
     * 握手前处理方法的参数信息数组
     */
    private final MethodParameter[] beforeHandshakeParameters;

    /**
     * 连接打开处理方法的参数信息数组
     */
    private final MethodParameter[] onOpenParameters;

    /**
     * 连接关闭处理方法的参数信息数组
     */
    private final MethodParameter[] onCloseParameters;

    /**
     * 异常处理方法的参数信息数组
     */
    private final MethodParameter[] onErrorParameters;

    /**
     * 文本消息处理方法的参数信息数组
     */
    private final MethodParameter[] onMessageParameters;

    /**
     * 二进制消息处理方法的参数信息数组
     */
    private final MethodParameter[] onBinaryParameters;

    /**
     * 事件处理方法的参数信息数组
     */
    private final MethodParameter[] onEventParameters;

    /**
     * 握手前处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] beforeHandshakeArgResolvers;

    /**
     * 连接打开处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] onOpenArgResolvers;

    /**
     * 连接关闭处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] onCloseArgResolvers;

    /**
     * 异常处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] onErrorArgResolvers;

    /**
     * 文本消息处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] onMessageArgResolvers;

    /**
     * 二进制消息处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] onBinaryArgResolvers;

    /**
     * 事件处理方法的参数解析器数组
     */
    private final MethodArgumentResolver[] onEventArgResolvers;

    /**
     * WebSocket端点POJO类
     */
    private final Class pojoClazz;

    /**
     * Spring应用上下文
     */
    private final ApplicationContext applicationContext;

    /**
     * Spring抽象Bean工厂
     */
    private final AbstractBeanFactory beanFactory;

    /**
     * 构造函数，扫描并解析POJO类中的WebSocket注解方法
     *
     * @param pojoClazz WebSocket端点POJO类
     * @param context Spring应用上下文
     * @param beanFactory Spring抽象Bean工厂
     * @throws DeploymentException 部署异常
     */
    public PojoMethodMapping(Class<?> pojoClazz, ApplicationContext context, AbstractBeanFactory beanFactory) throws DeploymentException {
        this.applicationContext = context;
        this.pojoClazz = pojoClazz;
        this.beanFactory = beanFactory;
        Method handshake = null;
        Method open = null;
        Method close = null;
        Method error = null;
        Method message = null;
        Method binary = null;
        Method event = null;
        Method[] pojoClazzMethods = null;
        Class<?> currentClazz = pojoClazz;
        // 遍历类继承层次结构，扫描所有声明的方法
        while (!currentClazz.equals(Object.class)) {
            Method[] currentClazzMethods = currentClazz.getDeclaredMethods();
            if (currentClazz == pojoClazz) {
                pojoClazzMethods = currentClazzMethods;
            }
            // 扫描当前类的所有方法，识别WebSocket相关注解
            for (Method method : currentClazzMethods) {
                if (method.getAnnotation(BeforeHandshake.class) != null) {
                    checkPublic(method);
                    if (handshake == null) {
                        handshake = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(handshake, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation BeforeHandshake");
                        }
                    }
                } else if (method.getAnnotation(OnOpen.class) != null) {
                    checkPublic(method);
                    if (open == null) {
                        open = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(open, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnOpen");
                        }
                    }
                } else if (method.getAnnotation(OnClose.class) != null) {
                    checkPublic(method);
                    if (close == null) {
                        close = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(close, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnClose");
                        }
                    }
                } else if (method.getAnnotation(OnError.class) != null) {
                    checkPublic(method);
                    if (error == null) {
                        error = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(error, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnError");
                        }
                    }
                } else if (method.getAnnotation(OnMessage.class) != null) {
                    checkPublic(method);
                    if (message == null) {
                        message = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(message, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation onMessage");
                        }
                    }
                } else if (method.getAnnotation(OnBinary.class) != null) {
                    checkPublic(method);
                    if (binary == null) {
                        binary = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(binary, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnBinary");
                        }
                    }
                } else if (method.getAnnotation(OnEvent.class) != null) {
                    checkPublic(method);
                    if (event == null) {
                        event = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(event, method)) {
                            // 重复注解异常
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnEvent");
                        }
                    }
                } else {
                    // 未注解的方法
                }
            }
            currentClazz = currentClazz.getSuperclass();
        }
        // 如果方法不在pojoClazz上且被pojoClazz中的非注解方法覆盖，则应忽略
        if (handshake != null && handshake.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, handshake, BeforeHandshake.class)) {
                handshake = null;
            }
        }
        if (open != null && open.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, open, OnOpen.class)) {
                open = null;
            }
        }
        if (close != null && close.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, close, OnClose.class)) {
                close = null;
            }
        }
        if (error != null && error.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, error, OnError.class)) {
                error = null;
            }
        }
        if (message != null && message.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, message, OnMessage.class)) {
                message = null;
            }
        }
        if (binary != null && binary.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, binary, OnBinary.class)) {
                binary = null;
            }
        }
        if (event != null && event.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, event, OnEvent.class)) {
                event = null;
            }
        }

        this.beforeHandshake = handshake;
        this.onOpen = open;
        this.onClose = close;
        this.onError = error;
        this.onMessage = message;
        this.onBinary = binary;
        this.onEvent = event;
        // 初始化各方法的参数信息和参数解析器
        beforeHandshakeParameters = getParameters(beforeHandshake);
        onOpenParameters = getParameters(onOpen);
        onCloseParameters = getParameters(onClose);
        onMessageParameters = getParameters(onMessage);
        onErrorParameters = getParameters(onError);
        onBinaryParameters = getParameters(onBinary);
        onEventParameters = getParameters(onEvent);
        beforeHandshakeArgResolvers = getResolvers(beforeHandshakeParameters);
        onOpenArgResolvers = getResolvers(onOpenParameters);
        onCloseArgResolvers = getResolvers(onCloseParameters);
        onMessageArgResolvers = getResolvers(onMessageParameters);
        onErrorArgResolvers = getResolvers(onErrorParameters);
        onBinaryArgResolvers = getResolvers(onBinaryParameters);
        onEventArgResolvers = getResolvers(onEventParameters);
    }

    /**
     * 检查方法是否为public访问级别
     *
     * @param m 待检查的方法
     * @throws DeploymentException 如果方法不是public访问级别
     */
    private void checkPublic(Method m) throws DeploymentException {
        if (!Modifier.isPublic(m.getModifiers())) {
            throw new DeploymentException(
                    "pojoMethodMapping.methodNotPublic " + m.getName());
        }
    }

    /**
     * 判断两个方法是否为重写关系
     *
     * @param method1 方法1
     * @param method2 方法2
     * @return 是否为重写关系
     */
    private boolean isMethodOverride(Method method1, Method method2) {
        return (method1.getName().equals(method2.getName())
                && method1.getReturnType().equals(method2.getReturnType())
                && Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes()));
    }

    /**
     * 判断方法是否被无注解的方法覆盖
     *
     * @param methods 方法数组
     * @param superclazzMethod 父类方法
     * @param annotation 注解类型
     * @return 是否被无注解的方法覆盖
     */
    private boolean isOverridenWithoutAnnotation(Method[] methods, Method superclazzMethod, Class<? extends Annotation> annotation) {
        for (Method method : methods) {
            if (isMethodOverride(method, superclazzMethod)
                    && (method.getAnnotation(annotation) == null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取端点实例对象
     *
     * @return 端点实例对象
     * @throws NoSuchMethodException 无构造方法异常
     * @throws IllegalAccessException 非法访问异常
     * @throws InvocationTargetException 调用目标异常
     * @throws InstantiationException 实例化异常
     */
    Object getEndpointInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object implement = pojoClazz.getDeclaredConstructor().newInstance();
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(implement);
        return implement;
    }

    /**
     * 获取握手前处理方法
     *
     * @return 握手前处理方法
     */
    Method getBeforeHandshake() {
        return beforeHandshake;
    }

    /**
     * 获取握手前处理方法的参数值
     *
     * @param channel Netty通道
     * @param req 完整HTTP请求
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getBeforeHandshakeArgs(Channel channel, FullHttpRequest req) throws Exception {
        return getMethodArgumentValues(channel, req, beforeHandshakeParameters, beforeHandshakeArgResolvers);
    }

    /**
     * 获取连接打开处理方法
     *
     * @return 连接打开处理方法
     */
    Method getOnOpen() {
        return onOpen;
    }

    /**
     * 获取连接打开处理方法的参数值
     *
     * @param channel Netty通道
     * @param req 完整HTTP请求
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getOnOpenArgs(Channel channel, FullHttpRequest req) throws Exception {
        return getMethodArgumentValues(channel, req, onOpenParameters, onOpenArgResolvers);
    }

    /**
     * 获取连接打开处理方法的参数解析器
     *
     * @return 参数解析器数组
     */
    MethodArgumentResolver[] getOnOpenArgResolvers() {
        return onOpenArgResolvers;
    }

    /**
     * 获取连接关闭处理方法
     *
     * @return 连接关闭处理方法
     */
    Method getOnClose() {
        return onClose;
    }

    /**
     * 获取连接关闭处理方法的参数值
     *
     * @param channel Netty通道
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getOnCloseArgs(Channel channel) throws Exception {
        return getMethodArgumentValues(channel, null, onCloseParameters, onCloseArgResolvers);
    }

    /**
     * 获取异常处理方法
     *
     * @return 异常处理方法
     */
    Method getOnError() {
        return onError;
    }

    /**
     * 获取异常处理方法的参数值
     *
     * @param channel Netty通道
     * @param throwable 异常对象
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getOnErrorArgs(Channel channel, Throwable throwable) throws Exception {
        return getMethodArgumentValues(channel, throwable, onErrorParameters, onErrorArgResolvers);
    }

    /**
     * 获取文本消息处理方法
     *
     * @return 文本消息处理方法
     */
    Method getOnMessage() {
        return onMessage;
    }

    /**
     * 获取文本消息处理方法的参数值
     *
     * @param channel Netty通道
     * @param textWebSocketFrame 文本WebSocket帧
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getOnMessageArgs(Channel channel, TextWebSocketFrame textWebSocketFrame) throws Exception {
        return getMethodArgumentValues(channel, textWebSocketFrame, onMessageParameters, onMessageArgResolvers);
    }

    /**
     * 获取二进制消息处理方法
     *
     * @return 二进制消息处理方法
     */
    Method getOnBinary() {
        return onBinary;
    }

    /**
     * 获取二进制消息处理方法的参数值
     *
     * @param channel Netty通道
     * @param binaryWebSocketFrame 二进制WebSocket帧
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getOnBinaryArgs(Channel channel, BinaryWebSocketFrame binaryWebSocketFrame) throws Exception {
        return getMethodArgumentValues(channel, binaryWebSocketFrame, onBinaryParameters, onBinaryArgResolvers);
    }

    /**
     * 获取事件处理方法
     *
     * @return 事件处理方法
     */
    Method getOnEvent() {
        return onEvent;
    }

    /**
     * 获取事件处理方法的参数值
     *
     * @param channel Netty通道
     * @param evt 事件对象
     * @return 参数值数组
     * @throws Exception 异常
     */
    Object[] getOnEventArgs(Channel channel, Object evt) throws Exception {
        return getMethodArgumentValues(channel, evt, onEventParameters, onEventArgResolvers);
    }

    /**
     * 获取方法参数值
     *
     * @param channel Netty通道
     * @param object 参数对象
     * @param parameters 方法参数信息数组
     * @param resolvers 参数解析器数组
     * @return 参数值数组
     * @throws Exception 异常
     */
    private Object[] getMethodArgumentValues(Channel channel, Object object, MethodParameter[] parameters, MethodArgumentResolver[] resolvers) throws Exception {
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            MethodArgumentResolver resolver = resolvers[i];
            Object arg = resolver.resolveArgument(parameter, channel, object);
            objects[i] = arg;
        }
        return objects;
    }

    /**
     * 获取参数解析器
     *
     * @param parameters 方法参数信息数组
     * @return 参数解析器数组
     * @throws DeploymentException 部署异常
     */
    private MethodArgumentResolver[] getResolvers(MethodParameter[] parameters) throws DeploymentException {
        MethodArgumentResolver[] methodArgumentResolvers = new MethodArgumentResolver[parameters.length];
        List<MethodArgumentResolver> resolvers = getDefaultResolvers();
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            for (MethodArgumentResolver resolver : resolvers) {
                if (resolver.supportsParameter(parameter)) {
                    methodArgumentResolvers[i] = resolver;
                    break;
                }
            }
            if (methodArgumentResolvers[i] == null) {
                throw new DeploymentException("pojoMethodMapping.paramClassIncorrect parameter name : " + parameter.getParameterName());
            }
        }
        return methodArgumentResolvers;
    }

    /**
     * 获取默认的参数解析器列表
     *
     * @return 参数解析器列表
     */
    private List<MethodArgumentResolver> getDefaultResolvers() {
        List<MethodArgumentResolver> resolvers = new ArrayList<>();
        resolvers.add(new SessionMethodArgumentResolver());
        resolvers.add(new HttpHeadersMethodArgumentResolver());
        resolvers.add(new TextMethodArgumentResolver());
        resolvers.add(new ThrowableMethodArgumentResolver());
        resolvers.add(new ByteMethodArgumentResolver());
        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new RequestParamMethodArgumentResolver(beanFactory));
        resolvers.add(new PathVariableMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver(beanFactory));
        resolvers.add(new EventMethodArgumentResolver(beanFactory));
        return resolvers;
    }

    /**
     * 获取方法的参数信息数组
     *
     * @param m 方法对象
     * @return 方法参数信息数组
     */
    private static MethodParameter[] getParameters(Method m) {
        if (m == null) {
            return new MethodParameter[0];
        }
        int count = m.getParameterCount();
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            MethodParameter methodParameter = new MethodParameter(m, i);
            methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
            result[i] = methodParameter;
        }
        return result;
    }
}
