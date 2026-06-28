package com.bocoo.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_provider_credential")
public class AiProviderCredential extends BaseEntity {

    @TableId(value = "credential_id")
    private Long credentialId;

    private Long providerId;

    private String apiKeyCiphertext;

    private String keyFingerprint;

    private String status;

    private LocalDateTime lastUsedTime;

    private String remark;
}
