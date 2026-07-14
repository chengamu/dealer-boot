package com.bocoo.common.oss.core;

import cn.hutool.core.util.IdUtil;
import com.bocoo.common.core.utils.DateUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.oss.constant.OssConstant;
import com.bocoo.common.oss.properties.OssProperties;

/**
 * OSS URL 和对象路径支持。
 */
final class OssObjectPathSupport {

    private OssObjectPathSupport() {
    }

    static String url(OssProperties properties) {
        String domain = properties.getDomain();
        String endpoint = properties.getEndpoint();
        String header = OssConstant.IS_HTTPS.equals(properties.getIsHttps()) ? "https://" : "http://";
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            if (StringUtils.isNotBlank(domain)) {
                return header + domain;
            }
            return header + properties.getBucketName() + "." + endpoint;
        }
        if (StringUtils.isNotBlank(domain)) {
            return header + domain + "/" + properties.getBucketName();
        }
        return header + endpoint + "/" + properties.getBucketName();
    }

    static String path(String prefix, String suffix) {
        String path = DateUtils.datePath() + "/" + IdUtil.fastSimpleUUID();
        if (StringUtils.isNotBlank(prefix)) {
            path = prefix + "/" + path;
        }
        return path + suffix;
    }

    static String cleanObjectKey(OssProperties properties, String path) {
        return path.replace(url(properties) + "/", "");
    }
}
