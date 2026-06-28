package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiProviderCredential;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiProviderCredential.class)
public class AiProviderCredentialVo {
    private Long credentialId;
    private Long providerId;
    private String keyFingerprint;
    private String status;
    private LocalDateTime lastUsedTime;
    private String remark;
}
