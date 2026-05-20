package com.bocoo.common.websocket.standard;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import com.bocoo.common.websocket.annotation.ServerEndpoint;

import java.util.Set;

/**
 * WebSocket端点类路径扫描器
 *
 * 该类继承自Spring的ClassPathBeanDefinitionScanner，专门用于扫描标记了@ServerEndpoint注解的WebSocket端点类。
 * 它会在指定的包路径下查找所有带有@ServerEndpoint注解的类，并将其注册为Spring Bean定义。
 *
 * @author bocoo
 * @since 1.0.0
 */
public class EndpointClassPathScanner extends ClassPathBeanDefinitionScanner {

    /**
     * 构造函数，初始化端点类路径扫描器
     *
     * @param registry Bean定义注册器
     * @param useDefaultFilters 是否使用默认过滤器
     */
    public EndpointClassPathScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    /**
     * 执行扫描操作，查找并注册WebSocket端点Bean定义
     *
     * 该方法会添加一个注解类型过滤器，专门用于匹配@ServerEndpoint注解的类，
     * 然后调用父类的扫描方法完成实际的类路径扫描和Bean定义注册。
     *
     * @param basePackages 需要扫描的基础包路径数组
     * @return 扫描到的Bean定义持有者集合
     */
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        addIncludeFilter(new AnnotationTypeFilter(ServerEndpoint.class));
        return super.doScan(basePackages);
    }
}
