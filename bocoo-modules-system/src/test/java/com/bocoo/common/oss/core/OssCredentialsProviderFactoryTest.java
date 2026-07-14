package com.bocoo.common.oss.core;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.bocoo.common.oss.exception.OssException;
import com.bocoo.common.oss.properties.OssProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OssCredentialsProviderFactoryTest {

    @Test
    void localStsUsesOnlyCurrentProcessEnvironment() {
        OssProperties properties = properties("local_sts");
        Map<String, String> environment = Map.of(
            "ALIBABA_CLOUD_ACCESS_KEY_ID", "temporary-id",
            "ALIBABA_CLOUD_ACCESS_KEY_SECRET", "temporary-secret",
            "ALIBABA_CLOUD_SECURITY_TOKEN", "temporary-token"
        );

        AWSCredentials credentials = OssCredentialsProviderFactory
            .create(properties, environment::get)
            .getCredentials();

        assertThat(credentials.getAWSAccessKeyId()).isEqualTo("temporary-id");
        assertThat(credentials.getAWSSecretKey()).isEqualTo("temporary-secret");
        assertThat(credentials).isInstanceOf(AWSSessionCredentials.class);
        assertThat(((AWSSessionCredentials) credentials).getSessionToken()).isEqualTo("temporary-token");
    }

    @Test
    void localStsRequiresSecurityToken() {
        OssProperties properties = properties("local_sts");
        Map<String, String> environment = Map.of(
            "ALIBABA_CLOUD_ACCESS_KEY_ID", "temporary-id",
            "ALIBABA_CLOUD_ACCESS_KEY_SECRET", "temporary-secret"
        );

        assertThatThrownBy(() -> OssCredentialsProviderFactory.create(properties, environment::get))
            .isInstanceOf(OssException.class);
    }

    @Test
    void accessKeyModeKeepsStoredCredentials() {
        OssProperties properties = properties(null);
        properties.setAccessKey("legacy-id");
        properties.setSecretKey("legacy-secret");

        AWSCredentials credentials = OssCredentialsProviderFactory
            .create(properties, ignored -> null)
            .getCredentials();

        assertThat(credentials.getAWSAccessKeyId()).isEqualTo("legacy-id");
        assertThat(credentials.getAWSSecretKey()).isEqualTo("legacy-secret");
    }

    private OssProperties properties(String mode) {
        OssProperties properties = new OssProperties();
        properties.setExt1(mode);
        return properties;
    }
}
