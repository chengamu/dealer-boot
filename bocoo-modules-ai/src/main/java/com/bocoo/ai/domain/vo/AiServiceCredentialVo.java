package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiServiceCredential;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiServiceCredential.class)
public class AiServiceCredentialVo {
    private Long credentialId;
    private String serviceName;
    private String keyVersion;
    private String secretFingerprint;
    private String status;
    private LocalDateTime lastUsedTime;
    private String remark;
    private LocalDateTime createTime;
}
