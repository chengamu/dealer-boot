package com.bocoo.common.oss.core;

import com.aliyun.credentials.Client;
import com.aliyun.credentials.models.CredentialModel;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.bocoo.common.oss.exception.OssException;

/**
 * 将阿里云临时凭证适配为 AWS SDK 1.x 凭证。
 */
public final class AliyunAwsCredentialsProvider implements AWSCredentialsProvider {

    private final Client credentialClient;

    public AliyunAwsCredentialsProvider(Client credentialClient) {
        this.credentialClient = credentialClient;
    }

    @Override
    public AWSCredentials getCredentials() {
        try {
            CredentialModel credential = credentialClient.getCredential();
            if (isBlank(credential.getAccessKeyId())
                || isBlank(credential.getAccessKeySecret())
                || isBlank(credential.getSecurityToken())) {
                throw new OssException("阿里云临时凭证不完整");
            }
            return new BasicSessionCredentials(
                credential.getAccessKeyId(),
                credential.getAccessKeySecret(),
                credential.getSecurityToken()
            );
        } catch (OssException e) {
            throw e;
        } catch (Exception e) {
            throw new OssException("获取阿里云临时凭证失败");
        }
    }

    @Override
    public void refresh() {
        // credentials-java 在 getCredential() 内部负责缓存和自动刷新。
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
