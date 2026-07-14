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
    void localStsDefersEnvironmentValidationUntilCredentialsAreRequested() {
        OssProperties properties = properties("local_sts");
        Map<String, String> environment = Map.of();

        var provider = OssCredentialsProviderFactory.create(properties, environment::get);

        assertThatThrownBy(provider::getCredentials)
            .isInstanceOf(OssException.class)
            .hasMessage("缺少环境变量: ALIBABA_CLOUD_ACCESS_KEY_ID");
    }

    @Test
    void ecsRamRoleDefersEnvironmentValidationUntilCredentialsAreRequested() {
        OssProperties properties = properties("ecs_ram_role");

        var provider = OssCredentialsProviderFactory.create(properties, ignored -> null);

        assertThatThrownBy(provider::getCredentials)
            .isInstanceOf(OssException.class)
            .hasMessage("缺少环境变量: ALIBABA_CLOUD_ECS_METADATA");
    }

    @Test
    void localStsClientCanBuildPublicUrlWithoutCredentials() {
        OssProperties properties = properties("local_sts");
        properties.setEndpoint("https://s3.oss-cn-hongkong.aliyuncs.com");
        properties.setRegion("cn-hongkong");
        properties.setBucketName("bucket");
        properties.setDomain("assets.example.com");
        properties.setIsHttps("Y");
        properties.setAccessPolicy("1");

        OssClient client = new OssClient("aliyun-oss", properties);

        assertThat(client.getUrl()).isEqualTo("https://assets.example.com");
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
