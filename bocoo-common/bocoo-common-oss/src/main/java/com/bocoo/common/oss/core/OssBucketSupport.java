package com.bocoo.common.oss.core;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bocoo.common.oss.enumd.AccessPolicyType;
import com.bocoo.common.oss.enumd.PolicyType;
import com.bocoo.common.oss.exception.OssException;
import com.bocoo.common.oss.properties.OssProperties;

/**
 * Bucket 初始化和对象 ACL 支持。
 */
final class OssBucketSupport {

    private OssBucketSupport() {
    }

    static void initialize(AmazonS3 client, OssProperties properties) {
        if (mode(properties).usesSharedBucket()) {
            return;
        }
        try {
            String bucketName = properties.getBucketName();
            if (client.doesBucketExistV2(bucketName)) {
                return;
            }
            AccessPolicyType accessPolicy = accessPolicy(properties);
            CreateBucketRequest request = new CreateBucketRequest(bucketName);
            request.setCannedAcl(accessPolicy.getAcl());
            client.createBucket(request);
            client.setBucketPolicy(bucketName, policy(bucketName, accessPolicy.getPolicyType()));
        } catch (Exception e) {
            throw new OssException("创建Bucket失败, 请核对配置信息:[" + e.getMessage() + "]");
        }
    }

    static void applyObjectAcl(PutObjectRequest request, OssProperties properties) {
        if (!mode(properties).usesSharedBucket()) {
            request.setCannedAcl(accessPolicy(properties).getAcl());
        }
    }

    private static OssCredentialMode mode(OssProperties properties) {
        try {
            return OssCredentialMode.from(properties.getExt1());
        } catch (IllegalArgumentException e) {
            throw new OssException(e.getMessage());
        }
    }

    private static AccessPolicyType accessPolicy(OssProperties properties) {
        return AccessPolicyType.getByType(properties.getAccessPolicy());
    }

    private static String policy(String bucketName, PolicyType policyType) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n\"Statement\": [\n{\n\"Action\": [\n");
        builder.append(switch (policyType) {
            case WRITE -> "\"s3:GetBucketLocation\",\n\"s3:ListBucketMultipartUploads\"\n";
            case READ_WRITE -> "\"s3:GetBucketLocation\",\n\"s3:ListBucket\",\n\"s3:ListBucketMultipartUploads\"\n";
            default -> "\"s3:GetBucketLocation\"\n";
        });
        builder.append("],\n\"Effect\": \"Allow\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::")
            .append(bucketName).append("\"\n},\n");
        if (policyType == PolicyType.READ) {
            builder.append("{\n\"Action\": [\n\"s3:ListBucket\"\n],\n\"Effect\": \"Deny\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::")
                .append(bucketName).append("\"\n},\n");
        }
        builder.append("{\n\"Action\": ");
        builder.append(switch (policyType) {
            case WRITE -> "[\n\"s3:AbortMultipartUpload\",\n\"s3:DeleteObject\",\n\"s3:ListMultipartUploadParts\",\n\"s3:PutObject\"\n],\n";
            case READ_WRITE -> "[\n\"s3:AbortMultipartUpload\",\n\"s3:DeleteObject\",\n\"s3:GetObject\",\n\"s3:ListMultipartUploadParts\",\n\"s3:PutObject\"\n],\n";
            default -> "\"s3:GetObject\",\n";
        });
        return builder.append("\"Effect\": \"Allow\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::")
            .append(bucketName).append("/*\"\n}\n],\n\"Version\": \"2012-10-17\"\n}\n")
            .toString();
    }
}
