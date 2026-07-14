package com.bocoo.common.oss.core;

import com.aliyun.credentials.Client;
import com.aliyun.credentials.models.Config;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.bocoo.common.oss.exception.OssException;
import com.bocoo.common.oss.properties.OssProperties;

import java.util.function.Function;

/**
 * 根据 OSS 配置创建 AWS SDK 凭证 Provider。
 */
public final class OssCredentialsProviderFactory {

    private static final String ACCESS_KEY_ID = "ALIBABA_CLOUD_ACCESS_KEY_ID";
    private static final String ACCESS_KEY_SECRET = "ALIBABA_CLOUD_ACCESS_KEY_SECRET";
    private static final String SECURITY_TOKEN = "ALIBABA_CLOUD_SECURITY_TOKEN";
    private static final String ECS_ROLE_NAME = "ALIBABA_CLOUD_ECS_METADATA";

    private OssCredentialsProviderFactory() {
    }

    public static AWSCredentialsProvider create(OssProperties properties) {
        return create(properties, System::getenv);
    }

    static AWSCredentialsProvider create(OssProperties properties, Function<String, String> environment) {
        OssCredentialMode mode = parseMode(properties.getExt1());
        return switch (mode) {
            case ACCESS_KEY -> staticProvider(properties);
            case LOCAL_STS -> aliyunProvider(localStsConfig(environment));
            case ECS_RAM_ROLE -> aliyunProvider(ecsRamRoleConfig(environment));
        };
    }

    private static AWSCredentialsProvider staticProvider(OssProperties properties) {
        AWSCredentials credentials = new BasicAWSCredentials(properties.getAccessKey(), properties.getSecretKey());
        return new AWSStaticCredentialsProvider(credentials);
    }

    private static AWSCredentialsProvider aliyunProvider(Config config) {
        return new AliyunAwsCredentialsProvider(new Client(config));
    }

    private static Config localStsConfig(Function<String, String> environment) {
        return new Config()
            .setType("sts")
            .setAccessKeyId(required(environment, ACCESS_KEY_ID))
            .setAccessKeySecret(required(environment, ACCESS_KEY_SECRET))
            .setSecurityToken(required(environment, SECURITY_TOKEN));
    }

    private static Config ecsRamRoleConfig(Function<String, String> environment) {
        return new Config()
            .setType("ecs_ram_role")
            .setRoleName(required(environment, ECS_ROLE_NAME))
            .setDisableIMDSv1(true);
    }

    private static String required(Function<String, String> environment, String name) {
        String value = environment.apply(name);
        if (value == null || value.isBlank()) {
            throw new OssException("缺少环境变量: " + name);
        }
        return value;
    }

    private static OssCredentialMode parseMode(String value) {
        try {
            return OssCredentialMode.from(value);
        } catch (IllegalArgumentException e) {
            throw new OssException(e.getMessage());
        }
    }
}
