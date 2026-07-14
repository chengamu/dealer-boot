package com.bocoo.system.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.oss.core.OssCredentialMode;
import com.bocoo.system.domain.entity.SysOssConfig;

/**
 * OSS 配置凭证保存规则。
 */
final class OssConfigCredentialSupport {

    private OssConfigCredentialSupport() {
    }

    static OssCredentialMode prepare(SysOssConfig entity) {
        OssCredentialMode mode;
        try {
            mode = OssCredentialMode.from(entity.getExt1());
        } catch (IllegalArgumentException e) {
            throw ServiceException.ofMessageKey("oss.config.authMode.invalid");
        }
        entity.setExt1(mode.getValue());
        if (mode.usesSharedBucket()) {
            entity.setAccessKey(null);
            entity.setSecretKey(null);
            return mode;
        }
        validateStoredCredential(
            entity.getAccessKey(),
            "validation.oss.accessKey.required",
            "validation.oss.accessKey.size"
        );
        validateStoredCredential(
            entity.getSecretKey(),
            "validation.oss.secretKey.required",
            "validation.oss.secretKey.size"
        );
        return mode;
    }

    private static void validateStoredCredential(String value, String requiredKey, String sizeKey) {
        if (StringUtils.isBlank(value)) {
            throw ServiceException.ofMessageKey(requiredKey);
        }
        if (value.length() < 2 || value.length() > 100) {
            throw ServiceException.ofMessageKey(sizeKey);
        }
    }
}
