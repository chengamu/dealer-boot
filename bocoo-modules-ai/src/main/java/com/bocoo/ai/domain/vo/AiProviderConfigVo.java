package com.bocoo.ai.domain.vo;

import com.bocoo.ai.domain.entity.AiProviderConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiProviderConfig.class)
public class AiProviderConfigVo {
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String baseUrl;
    private String chatCompletionsPath;
    private String defaultModel;
    private Integer timeoutSeconds;
    private Boolean enabled;
    private String status;
    private String remark;
    private String keyFingerprint;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
