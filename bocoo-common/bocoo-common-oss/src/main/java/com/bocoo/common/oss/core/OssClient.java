package com.bocoo.common.oss.core;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.bocoo.common.core.utils.DateUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.oss.constant.OssConstant;
import com.bocoo.common.oss.entity.UploadResult;
import com.bocoo.common.oss.enumd.AccessPolicyType;
import com.bocoo.common.oss.enumd.PolicyType;
import com.bocoo.common.oss.exception.OssException;
import com.bocoo.common.oss.properties.OssProperties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * OssClient 类用于与对象存储服务（如 Amazon S3 或 MinIO）进行交互。
 * 提供了创建 Bucket、上传文件、删除文件、获取文件元数据等功能。
 * @author cmx
 */
public class OssClient {

    private final String configKey;

    private final OssProperties properties;

    private final AmazonS3 client;

    /**
     * 构造一个 OssClient 实例。
     *
     * @param configKey       配置键，用于标识该客户端实例
     * @param ossProperties   包含 OSS 配置信息的对象
     * @throws OssException 如果初始化过程中出现错误
     */
    public OssClient(String configKey, OssProperties ossProperties) {
        this.configKey = configKey;
        this.properties = ossProperties;
        try {
            AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(), properties.getRegion());

            AWSCredentials credentials = new BasicAWSCredentials(properties.getAccessKey(), properties.getSecretKey());
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            ClientConfiguration clientConfig = new ClientConfiguration();
            if (OssConstant.IS_HTTPS.equals(properties.getIsHttps())) {
                clientConfig.setProtocol(Protocol.HTTPS);
            } else {
                clientConfig.setProtocol(Protocol.HTTP);
            }
            AmazonS3ClientBuilder build = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(clientConfig)
                .withCredentials(credentialsProvider)
                .disableChunkedEncoding();
            if (!StringUtils.containsAny(properties.getEndpoint(), OssConstant.CLOUD_SERVICE)) {
                // minio 使用https限制使用域名访问 需要此配置 站点填域名
                build.enablePathStyleAccess();
            }
            this.client = build.build();

            createBucket();
        } catch (Exception e) {
            if (e instanceof OssException) {
                throw e;
            }
            throw new OssException("配置错误! 请检查系统配置:[" + e.getMessage() + "]");
        }
    }

    /**
     * 创建 Bucket，如果已存在则不执行任何操作。
     *
     * @throws OssException 如果创建 Bucket 失败
     */
    public void createBucket() {
        try {
            String bucketName = properties.getBucketName();
            if (client.doesBucketExistV2(bucketName)) {
                return;
            }
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            AccessPolicyType accessPolicy = getAccessPolicy();
            createBucketRequest.setCannedAcl(accessPolicy.getAcl());
            client.createBucket(createBucketRequest);
            client.setBucketPolicy(bucketName, getPolicy(bucketName, accessPolicy.getPolicyType()));
        } catch (Exception e) {
            throw new OssException("创建Bucket失败, 请核对配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 上传字节数组到指定路径。
     *
     * @param data        要上传的数据
     * @param path        文件在存储桶中的路径
     * @param contentType 内容类型
     * @return UploadResult 包含上传结果的信息
     */
    public UploadResult upload(byte[] data, String path, String contentType) {
        return upload(new ByteArrayInputStream(data), path, contentType);
    }

    /**
     * 上传输入流到指定路径。
     *
     * @param inputStream 输入流
     * @param path        文件在存储桶中的路径
     * @param contentType 内容类型
     * @return UploadResult 包含上传结果的信息
     */
    public UploadResult upload(InputStream inputStream, String path, String contentType) {
        InputStream processedStream = inputStream;
        long contentLength = 0;

        try {
            if (!(inputStream instanceof ByteArrayInputStream)) {
                byte[] bytes = IoUtil.readBytes(inputStream);
                contentLength = bytes.length;
                processedStream = new ByteArrayInputStream(bytes);
            } else {
                contentLength = inputStream.available();
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLength);
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucketName(), path, processedStream, metadata);
            // 设置上传对象的 Acl 为公共读
            putObjectRequest.setCannedAcl(getAccessPolicy().getAcl());
            client.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        } finally {
            try {
                if (processedStream != null) {
                    processedStream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return UploadResult.builder().url(getUrl() + "/" + path).filename(path).build();
    }

    /**
     * 上传文件到指定路径。
     *
     * @param file 文件对象
     * @param path 文件在存储桶中的路径
     * @return UploadResult 包含上传结果的信息
     */
    public UploadResult upload(File file, String path) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(properties.getBucketName(), path, file);
            // 设置上传对象的 Acl 为公共读
            putObjectRequest.setCannedAcl(getAccessPolicy().getAcl());
            client.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new OssException("上传文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
        return UploadResult.builder().url(getUrl() + "/" + path).filename(path).build();
    }

    /**
     * 删除指定路径的文件。
     *
     * @param path 文件在存储桶中的完整路径
     */
    public void delete(String path) {
        String cleanPath = path.replace(getUrl() + "/", "");
        try {
            client.deleteObject(properties.getBucketName(), cleanPath);
        } catch (Exception e) {
            throw new OssException("删除文件失败，请检查配置信息:[" + e.getMessage() + "]");
        }
    }

    /**
     * 根据后缀上传字节数组。
     *
     * @param data        要上传的数据
     * @param suffix      文件后缀名
     * @param contentType 内容类型
     * @return UploadResult 包含上传结果的信息
     */
    public UploadResult uploadSuffix(byte[] data, String suffix, String contentType) {
        return upload(data, getPath(properties.getPrefix(), suffix), contentType);
    }

    /**
     * 根据后缀上传输入流。
     *
     * @param inputStream 输入流
     * @param suffix      文件后缀名
     * @param contentType 内容类型
     * @return UploadResult 包含上传结果的信息
     */
    public UploadResult uploadSuffix(InputStream inputStream, String suffix, String contentType) {
        return upload(inputStream, getPath(properties.getPrefix(), suffix), contentType);
    }

    /**
     * 根据后缀上传文件。
     *
     * @param file   文件对象
     * @param suffix 文件后缀名
     * @return UploadResult 包含上传结果的信息
     */
    public UploadResult uploadSuffix(File file, String suffix) {
        return upload(file, getPath(properties.getPrefix(), suffix));
    }

    /**
     * 获取文件元数据。
     *
     * @param path 完整文件路径
     * @return ObjectMetadata 文件元数据
     */
    public ObjectMetadata getObjectMetadata(String path) {
        String cleanPath = path.replace(getUrl() + "/", "");
        S3Object object = null;
        try {
            object = client.getObject(properties.getBucketName(), cleanPath);
            return object.getObjectMetadata();
        } finally {
            if (object != null) {
                try {
                    object.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 获取文件内容的输入流。
     *
     * @param path 完整文件路径
     * @return InputStream 文件内容的输入流
     */
    public InputStream getObjectContent(String path) {
        String cleanPath = path.replace(getUrl() + "/", "");
        S3Object object = client.getObject(properties.getBucketName(), cleanPath);
        return object.getObjectContent();
    }

    /**
     * 获取当前配置的访问地址。
     *
     * @return String 访问地址
     */
    public String getUrl() {
        String domain = properties.getDomain();
        String endpoint = properties.getEndpoint();
        String header = OssConstant.IS_HTTPS.equals(properties.getIsHttps()) ? "https://" : "http://";
        // 云服务商直接返回
        if (StringUtils.containsAny(endpoint, OssConstant.CLOUD_SERVICE)) {
            if (StringUtils.isNotBlank(domain)) {
                return header + domain;
            }
            return header + properties.getBucketName() + "." + endpoint;
        }
        // minio 单独处理
        if (StringUtils.isNotBlank(domain)) {
            return header + domain + "/" + properties.getBucketName();
        }
        return header + endpoint + "/" + properties.getBucketName();
    }

    /**
     * 构建文件路径。
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @return String 构建后的路径
     */
    public String getPath(String prefix, String suffix) {
        // 生成uuid
        String uuid = IdUtil.fastSimpleUUID();
        // 文件路径
        String path = DateUtils.datePath() + "/" + uuid;
        if (StringUtils.isNotBlank(prefix)) {
            path = prefix + "/" + path;
        }
        return path + suffix;
    }

    /**
     * 获取配置键。
     *
     * @return String 配置键
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 获取私有URL链接。
     *
     * @param objectKey 对象KEY
     * @param second    授权时间（秒）
     * @return String 私有URL链接
     */
    public String getPrivateUrl(String objectKey, Integer second) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(properties.getBucketName(), objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(new Date(System.currentTimeMillis() + 1000L * second));
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    /**
     * 检查配置是否相同。
     *
     * @param properties 需要比较的配置属性
     * @return boolean 是否相同
     */
    public boolean checkPropertiesSame(OssProperties properties) {
        return this.properties.equals(properties);
    }

    /**
     * 获取当前桶权限类型。
     *
     * @return AccessPolicyType 当前桶权限类型
     */
    public AccessPolicyType getAccessPolicy() {
        return AccessPolicyType.getByType(properties.getAccessPolicy());
    }

    /**
     * 根据指定的存储桶名称和策略类型生成对应的S3访问策略JSON字符串。
     *
     * @param bucketName  存储桶名称，用于构建资源ARN
     * @param policyType  策略类型（读、写、读写），决定允许或拒绝的操作权限
     * @return 返回符合AWS S3策略格式的JSON字符串
     */
    private static String getPolicy(String bucketName, PolicyType policyType) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n\"Statement\": [\n{\n\"Action\": [\n");

        // 根据策略类型设置对存储桶的基本操作权限
        builder.append(switch (policyType) {
            case WRITE -> "\"s3:GetBucketLocation\",\n\"s3:ListBucketMultipartUploads\"\n";
            case READ_WRITE -> "\"s3:GetBucketLocation\",\n\"s3:ListBucket\",\n\"s3:ListBucketMultipartUploads\"\n";
            default -> "\"s3:GetBucketLocation\"\n";
        });
        builder.append("],\n\"Effect\": \"Allow\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::");
        builder.append(bucketName);
        builder.append("\"\n},\n");

        // 如果是只读策略，则添加显式的拒绝写相关操作的规则
        if (policyType == PolicyType.READ) {
            builder.append("{\n\"Action\": [\n\"s3:ListBucket\"\n],\n\"Effect\": \"Deny\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::");
            builder.append(bucketName);
            builder.append("\"\n},\n");
        }

        // 设置针对对象的具体操作权限
        builder.append("{\n\"Action\": ");
        builder.append(switch (policyType) {
            case WRITE -> "[\n\"s3:AbortMultipartUpload\",\n\"s3:DeleteObject\",\n\"s3:ListMultipartUploadParts\",\n\"s3:PutObject\"\n],\n";
            case READ_WRITE -> "[\n\"s3:AbortMultipartUpload\",\n\"s3:DeleteObject\",\n\"s3:GetObject\",\n\"s3:ListMultipartUploadParts\",\n\"s3:PutObject\"\n],\n";
            default -> "\"s3:GetObject\",\n";
        });
        builder.append("\"Effect\": \"Allow\",\n\"Principal\": \"*\",\n\"Resource\": \"arn:aws:s3:::");
        builder.append(bucketName);
        builder.append("/*\"\n}\n],\n\"Version\": \"2012-10-17\"\n}\n");

        return builder.toString();
    }
}

