package com.bocoo.common.oss.core;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bocoo.common.oss.enumd.AccessPolicyType;
import com.bocoo.common.oss.properties.OssProperties;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OssBucketSupportTest {

    @Test
    void sharedBucketModeSkipsBucketInitialization() {
        AmazonS3 client = mock(AmazonS3.class);
        OssProperties properties = properties("local_sts");

        OssBucketSupport.initialize(client, properties);

        verifyNoInteractions(client);
    }

    @Test
    void legacyModeKeepsExistingBucketInitialization() {
        AmazonS3 client = mock(AmazonS3.class);
        OssProperties properties = properties(null);
        when(client.doesBucketExistV2("bucket")).thenReturn(true);

        OssBucketSupport.initialize(client, properties);

        verify(client).doesBucketExistV2("bucket");
    }

    @Test
    void sharedBucketUploadDoesNotSetObjectAcl() {
        OssProperties properties = properties("ecs_ram_role");
        PutObjectRequest request = request();

        OssBucketSupport.applyObjectAcl(request, properties);

        assertThat(request.getCannedAcl()).isNull();
    }

    @Test
    void legacyUploadKeepsConfiguredObjectAcl() {
        OssProperties properties = properties("access_key");
        PutObjectRequest request = request();

        OssBucketSupport.applyObjectAcl(request, properties);

        assertThat(request.getCannedAcl()).isEqualTo(AccessPolicyType.getByType("1").getAcl());
    }

    private OssProperties properties(String mode) {
        OssProperties properties = new OssProperties();
        properties.setExt1(mode);
        properties.setBucketName("bucket");
        properties.setAccessPolicy("1");
        return properties;
    }

    private PutObjectRequest request() {
        return new PutObjectRequest(
            "bucket",
            "key",
            new ByteArrayInputStream(new byte[0]),
            new ObjectMetadata()
        );
    }
}
