package com.bocoo.ai.domain.bo;

import lombok.Data;

@Data
public class AiProviderConfigBo {
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
}
