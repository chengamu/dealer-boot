package com.bocoo.common.security.handler;

import cn.hutool.core.util.ReUtil;
import com.bocoo.common.core.utils.SpringUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 获取所有Url配置
 *
 * @author cmx
 */
@Data
public class AllUrlHandler implements InitializingBean {

    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private List<String> urls = new ArrayList<>();

    /**
     * 在Bean属性设置完成后执行，用于获取所有URL映射信息
     * 该方法会在Spring容器初始化时自动调用
     */
    @Override
    public void afterPropertiesSet() {
        Set<String> set = new HashSet<>();
        RequestMappingHandlerMapping mapping = SpringUtils.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        // 遍历所有请求映射信息，提取URL路径并处理路径变量
        map.keySet().forEach(info -> {
            // 获取注解上边的 path 替代 path variable 为 *
            Objects.requireNonNull(info.getPathPatternsCondition().getPatterns())
                    .forEach(url -> set.add(ReUtil.replaceAll(url.getPatternString(), PATTERN, "*")));
        });
        urls.addAll(set);
    }

}

