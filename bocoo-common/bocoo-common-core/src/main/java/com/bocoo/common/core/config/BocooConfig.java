package com.bocoo.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * @author cmx
 */
@Data
@Component
@ConfigurationProperties(prefix = "bocoo")
public class BocooConfig {

    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 缓存懒加载
     */
    private boolean cacheLazy;

    /** 上传路径 */
    private String profile;


    /**
     * 获取头像上传路径
     */
    public String getAvatarPath() {
        // 添加 null 检查以避免 NullPointerException
        if (this.getProfile() == null) {
            return null;
        }
        return this.getProfile() + "/avatar";
    }
}
