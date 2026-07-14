package com.bocoo.system.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.oss.core.OssCredentialMode;
import com.bocoo.system.domain.entity.SysOssConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OssConfigCredentialSupportTest {

    @Test
    void temporaryCredentialModeClearsStoredKeys() {
        SysOssConfig config = validConfig();
        config.setExt1("local_sts");
        config.setAccessKey("must-not-be-stored");
        config.setSecretKey("must-not-be-stored");

        OssCredentialMode mode = OssConfigCredentialSupport.prepare(config);

        assertThat(mode).isEqualTo(OssCredentialMode.LOCAL_STS);
        assertThat(config.getAccessKey()).isNull();
        assertThat(config.getSecretKey()).isNull();
    }

    @Test
    void legacyModeStillRequiresStoredKeys() {
        SysOssConfig config = validConfig();
        config.setExt1(null);
        config.setAccessKey(null);
        config.setSecretKey(null);

        assertThatThrownBy(() -> OssConfigCredentialSupport.prepare(config))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void unknownCredentialModeIsRejected() {
        SysOssConfig config = validConfig();
        config.setExt1("unknown");

        assertThatThrownBy(() -> OssConfigCredentialSupport.prepare(config))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void blankLegacyModeIsNormalized() {
        SysOssConfig config = validConfig();
        config.setExt1(null);

        OssConfigCredentialSupport.prepare(config);

        assertThat(config.getExt1()).isEqualTo("access_key");
        assertThat(config.getAccessKey()).isEqualTo("legacy-id");
    }

    private SysOssConfig validConfig() {
        SysOssConfig config = new SysOssConfig();
        config.setConfigKey("aliyun-oss");
        config.setAccessKey("legacy-id");
        config.setSecretKey("legacy-secret");
        return config;
    }
}
