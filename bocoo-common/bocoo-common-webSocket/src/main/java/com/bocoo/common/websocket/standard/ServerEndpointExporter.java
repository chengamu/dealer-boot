package com.bocoo.common.websocket.standard;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import com.bocoo.common.websocket.annotation.EnableWebSocket;
import com.bocoo.common.websocket.annotation.ServerEndpoint;
import com.bocoo.common.websocket.exception.DeploymentException;
import com.bocoo.common.websocket.pojo.PojoEndpointServer;
import com.bocoo.common.websocket.pojo.PojoMethodMapping;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * WebSocket端点导出器
 *
 * 该类负责扫描、注册和初始化WebSocket端点，是整个WebSocket框架的核心组件。
 * 它实现了Spring的生命周期接口，在Spring容器初始化完成后自动注册所有WebSocket端点。
 *
 * @author Yeauty
 */
public class ServerEndpointExporter extends ApplicationObjectSupport implements SmartInitializingSingleton, BeanFactoryAware, ResourceLoaderAware {

    /**
     * Spring环境配置
     */
    @Autowired
    Environment environment;

    /**
     * Spring抽象Bean工厂
     */
    private AbstractBeanFactory beanFactory;

    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;

    /**
     * 地址与WebSocket服务器映射关系Map
     */
    private final Map<InetSocketAddress, WebsocketServer> addressWebsocketServerMap = new HashMap<>();

    /**
     * 所有单例Bean实例化完成后调用
     *
     * 该方法是SmartInitializingSingleton接口的回调方法，
     * 在Spring容器中所有单例Bean实例化完成后自动调用。
     */
    @Override
    public void afterSingletonsInstantiated() {
        registerEndpoints();
    }

    /**
     * 设置Bean工厂
     *
     * 该方法是BeanFactoryAware接口的回调方法，
     * 用于注入Spring的Bean工厂实例。
     *
     * @param beanFactory Bean工厂实例
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof AbstractBeanFactory)) {
            throw new IllegalArgumentException(
                    "AutowiredAnnotationBeanPostProcessor requires a AbstractBeanFactory: " + beanFactory);
        }
        this.beanFactory = (AbstractBeanFactory) beanFactory;
    }

    /**
     * 注册WebSocket端点
     *
     * 扫描所有标记了@ServerEndpoint注解的类，创建对应的端点配置和映射关系，
     * 并初始化WebSocket服务器。
     */
    protected void registerEndpoints() {
        ApplicationContext context = getApplicationContext();

        scanPackage(context);

        String[] endpointBeanNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
        Set<Class<?>> endpointClasses = new LinkedHashSet<>();
        for (String beanName : endpointBeanNames) {
            endpointClasses.add(context.getType(beanName));
        }

        for (Class<?> endpointClass : endpointClasses) {
            if (ClassUtils.isCglibProxyClass(endpointClass)) {
                registerEndpoint(endpointClass.getSuperclass());
            } else {
                registerEndpoint(endpointClass);
            }
        }

        init();
    }

    /**
     * 扫描包路径查找WebSocket端点类
     *
     * 根据@EnableWebSocket或@SpringBootApplication注解中的配置，
     * 确定需要扫描的包路径，并执行扫描操作。
     *
     * @param context Spring应用上下文
     */
    private void scanPackage(ApplicationContext context) {
        String[] basePackages = null;

        String[] enableWebSocketBeanNames = context.getBeanNamesForAnnotation(EnableWebSocket.class);
        if (enableWebSocketBeanNames.length != 0) {
            for (String enableWebSocketBeanName : enableWebSocketBeanNames) {
                Object enableWebSocketBean = context.getBean(enableWebSocketBeanName);
                EnableWebSocket enableWebSocket = AnnotationUtils.findAnnotation(enableWebSocketBean.getClass(), EnableWebSocket.class);
                assert enableWebSocket != null;
                if (enableWebSocket.scanBasePackages().length != 0) {
                    basePackages = enableWebSocket.scanBasePackages();
                    break;
                }
            }
        }

        // 使用@SpringBootApplication注解的包路径
        if (basePackages == null) {
            String[] springBootApplicationBeanName = context.getBeanNamesForAnnotation(SpringBootApplication.class);
            Object springBootApplicationBean = context.getBean(springBootApplicationBeanName[0]);
            SpringBootApplication springBootApplication = AnnotationUtils.findAnnotation(springBootApplicationBean.getClass(), SpringBootApplication.class);
            assert springBootApplication != null;
            if (springBootApplication.scanBasePackages().length != 0) {
                basePackages = springBootApplication.scanBasePackages();
            } else {
                String packageName = ClassUtils.getPackageName(springBootApplicationBean.getClass().getName());
                basePackages = new String[1];
                basePackages[0] = packageName;
            }
        }

        EndpointClassPathScanner scanHandle = new EndpointClassPathScanner((BeanDefinitionRegistry) context, false);
        if (resourceLoader != null) {
            scanHandle.setResourceLoader(resourceLoader);
        }

        for (String basePackage : basePackages) {
            scanHandle.doScan(basePackage);
        }
    }

    /**
     * 初始化WebSocket服务器
     *
     * 遍历所有已注册的WebSocket服务器，执行初始化操作并记录日志。
     */
    private void init() {
        for (Map.Entry<InetSocketAddress, WebsocketServer> entry : addressWebsocketServerMap.entrySet()) {
            WebsocketServer websocketServer = entry.getValue();
            try {
                websocketServer.init();
                PojoEndpointServer pojoEndpointServer = websocketServer.getPojoEndpointServer();
                StringJoiner stringJoiner = new StringJoiner(",");
                pojoEndpointServer.getPathMatcherSet().forEach(pathMatcher -> stringJoiner.add("'" + pathMatcher.getPattern() + "'"));
                logger.info(String.format("\033[34mNetty WebSocket started on port: %s with context path(s): %s .\033[0m", pojoEndpointServer.getPort(), stringJoiner.toString()));
            } catch (InterruptedException e) {
                logger.error(String.format("websocket [%s] init fail", entry.getKey()), e);
            } catch (SSLException e) {
                logger.error(String.format("websocket [%s] ssl create fail", entry.getKey()), e);

            }
        }
    }

    /**
     * 注册单个WebSocket端点
     *
     * 为指定的端点类创建配置信息和方法映射，并将其注册到对应的WebSocket服务器中。
     *
     * @param endpointClass WebSocket端点类
     */
    private void registerEndpoint(Class<?> endpointClass) {
        ServerEndpoint annotation = AnnotatedElementUtils.findMergedAnnotation(endpointClass, ServerEndpoint.class);
        if (annotation == null) {
            throw new IllegalStateException("missingAnnotation ServerEndpoint");
        }
        ServerEndpointConfig serverEndpointConfig = buildConfig(annotation);

        ApplicationContext context = getApplicationContext();
        PojoMethodMapping pojoMethodMapping = null;
        try {
            pojoMethodMapping = new PojoMethodMapping(endpointClass, context, beanFactory);
        } catch (DeploymentException e) {
            throw new IllegalStateException("Failed to register ServerEndpointConfig: " + serverEndpointConfig, e);
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverEndpointConfig.getHost(), serverEndpointConfig.getPort());
        String path = resolveAnnotationValue(annotation.value(), String.class, "path");

        WebsocketServer websocketServer = addressWebsocketServerMap.get(inetSocketAddress);
        if (websocketServer == null) {
            PojoEndpointServer pojoEndpointServer = new PojoEndpointServer(pojoMethodMapping, serverEndpointConfig, path);
            websocketServer = new WebsocketServer(pojoEndpointServer, serverEndpointConfig);
            addressWebsocketServerMap.put(inetSocketAddress, websocketServer);
        } else {
            websocketServer.getPojoEndpointServer().addPathPojoMethodMapping(path, pojoMethodMapping);
        }
    }

    /**
     * 构建服务器端点配置
     *
     * 根据@ServerEndpoint注解的属性值，解析并构建ServerEndpointConfig对象。
     *
     * @param annotation ServerEndpoint注解实例
     * @return 服务器端点配置对象
     */
    private ServerEndpointConfig buildConfig(ServerEndpoint annotation) {
        String host = resolveAnnotationValue(annotation.host(), String.class, "host");
        int port = resolveAnnotationValue(annotation.port(), Integer.class, "port");
        String path = resolveAnnotationValue(annotation.value(), String.class, "value");
        int bossLoopGroupThreads = resolveAnnotationValue(annotation.bossLoopGroupThreads(), Integer.class, "bossLoopGroupThreads");
        int workerLoopGroupThreads = resolveAnnotationValue(annotation.workerLoopGroupThreads(), Integer.class, "workerLoopGroupThreads");
        boolean useCompressionHandler = resolveAnnotationValue(annotation.useCompressionHandler(), Boolean.class, "useCompressionHandler");

        int optionConnectTimeoutMillis = resolveAnnotationValue(annotation.optionConnectTimeoutMillis(), Integer.class, "optionConnectTimeoutMillis");
        int optionSoBacklog = resolveAnnotationValue(annotation.optionSoBacklog(), Integer.class, "optionSoBacklog");

        int childOptionWriteSpinCount = resolveAnnotationValue(annotation.childOptionWriteSpinCount(), Integer.class, "childOptionWriteSpinCount");
        int childOptionWriteBufferHighWaterMark = resolveAnnotationValue(annotation.childOptionWriteBufferHighWaterMark(), Integer.class, "childOptionWriteBufferHighWaterMark");
        int childOptionWriteBufferLowWaterMark = resolveAnnotationValue(annotation.childOptionWriteBufferLowWaterMark(), Integer.class, "childOptionWriteBufferLowWaterMark");
        int childOptionSoRcvbuf = resolveAnnotationValue(annotation.childOptionSoRcvbuf(), Integer.class, "childOptionSoRcvbuf");
        int childOptionSoSndbuf = resolveAnnotationValue(annotation.childOptionSoSndbuf(), Integer.class, "childOptionSoSndbuf");
        boolean childOptionTcpNodelay = resolveAnnotationValue(annotation.childOptionTcpNodelay(), Boolean.class, "childOptionTcpNodelay");
        boolean childOptionSoKeepalive = resolveAnnotationValue(annotation.childOptionSoKeepalive(), Boolean.class, "childOptionSoKeepalive");
        int childOptionSoLinger = resolveAnnotationValue(annotation.childOptionSoLinger(), Integer.class, "childOptionSoLinger");
        boolean childOptionAllowHalfClosure = resolveAnnotationValue(annotation.childOptionAllowHalfClosure(), Boolean.class, "childOptionAllowHalfClosure");

        int readerIdleTimeSeconds = resolveAnnotationValue(annotation.readerIdleTimeSeconds(), Integer.class, "readerIdleTimeSeconds");
        int writerIdleTimeSeconds = resolveAnnotationValue(annotation.writerIdleTimeSeconds(), Integer.class, "writerIdleTimeSeconds");
        int allIdleTimeSeconds = resolveAnnotationValue(annotation.allIdleTimeSeconds(), Integer.class, "allIdleTimeSeconds");

        int maxFramePayloadLength = resolveAnnotationValue(annotation.maxFramePayloadLength(), Integer.class, "maxFramePayloadLength");

        boolean useEventExecutorGroup = resolveAnnotationValue(annotation.useEventExecutorGroup(), Boolean.class, "useEventExecutorGroup");
        int eventExecutorGroupThreads = resolveAnnotationValue(annotation.eventExecutorGroupThreads(), Integer.class, "eventExecutorGroupThreads");

        String sslKeyPassword = resolveAnnotationValue(annotation.sslKeyPassword(), String.class, "sslKeyPassword");
        String sslKeyStore = resolveAnnotationValue(annotation.sslKeyStore(), String.class, "sslKeyStore");
        String sslKeyStorePassword = resolveAnnotationValue(annotation.sslKeyStorePassword(), String.class, "sslKeyStorePassword");
        String sslKeyStoreType = resolveAnnotationValue(annotation.sslKeyStoreType(), String.class, "sslKeyStoreType");
        String sslTrustStore = resolveAnnotationValue(annotation.sslTrustStore(), String.class, "sslTrustStore");
        String sslTrustStorePassword = resolveAnnotationValue(annotation.sslTrustStorePassword(), String.class, "sslTrustStorePassword");
        String sslTrustStoreType = resolveAnnotationValue(annotation.sslTrustStoreType(), String.class, "sslTrustStoreType");

        String[] corsOrigins = annotation.corsOrigins();
        if (corsOrigins.length != 0) {
            for (int i = 0; i < corsOrigins.length; i++) {
                corsOrigins[i] = resolveAnnotationValue(corsOrigins[i], String.class, "corsOrigins");
            }
        }
        Boolean corsAllowCredentials = resolveAnnotationValue(annotation.corsAllowCredentials(), Boolean.class, "corsAllowCredentials");

        ServerEndpointConfig serverEndpointConfig = new ServerEndpointConfig(host, port, bossLoopGroupThreads, workerLoopGroupThreads
                , useCompressionHandler, optionConnectTimeoutMillis, optionSoBacklog, childOptionWriteSpinCount, childOptionWriteBufferHighWaterMark
                , childOptionWriteBufferLowWaterMark, childOptionSoRcvbuf, childOptionSoSndbuf, childOptionTcpNodelay, childOptionSoKeepalive
                , childOptionSoLinger, childOptionAllowHalfClosure, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds
                , maxFramePayloadLength, useEventExecutorGroup, eventExecutorGroupThreads
                , sslKeyPassword, sslKeyStore, sslKeyStorePassword, sslKeyStoreType
                , sslTrustStore, sslTrustStorePassword, sslTrustStoreType
                , corsOrigins, corsAllowCredentials);

        return serverEndpointConfig;
    }

    /**
     * 解析注解值
     *
     * 解析注解中的属性值，支持Spring的占位符解析和类型转换。
     *
     * @param value 原始值
     * @param requiredType 期望的类型
     * @param paramName 参数名称（用于错误提示）
     * @param <T> 返回值类型
     * @return 解析后的值
     */
    private <T> T resolveAnnotationValue(Object value, Class<T> requiredType, String paramName) {
        if (value == null) {
            return null;
        }
        TypeConverter typeConverter = beanFactory.getTypeConverter();

        if (value instanceof String) {
            String strVal = beanFactory.resolveEmbeddedValue((String) value);
            BeanExpressionResolver beanExpressionResolver = beanFactory.getBeanExpressionResolver();
            if (beanExpressionResolver != null) {
                value = beanExpressionResolver.evaluate(strVal, new BeanExpressionContext(beanFactory, null));
            } else {
                value = strVal;
            }
        }
        try {
            return typeConverter.convertIfNecessary(value, requiredType);
        } catch (TypeMismatchException e) {
            throw new IllegalArgumentException("Failed to convert value of parameter '" + paramName + "' to required type '" + requiredType.getName() + "'");
        }
    }

    /**
     * 设置资源加载器
     *
     * 该方法是ResourceLoaderAware接口的回调方法，
     * 用于注入Spring的资源加载器实例。
     *
     * @param resourceLoader 资源加载器实例
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
