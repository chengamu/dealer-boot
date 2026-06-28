package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_service_credential")
public class AiServiceCredential extends BaseEntity {

    @TableId(value = "credential_id")
    private Long credentialId;

    private String serviceName;

    private String keyVersion;

    private String secretCiphertext;

    private String secretFingerprint;

    private String status;

    private LocalDateTime lastUsedTime;

    private String remark;
}
