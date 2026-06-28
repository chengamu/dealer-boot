package com.bocoo.ai.domain.vo;

import lombok.Data;

@Data
public class AiRuntimeProviderVo {
    private String providerCode;
    private String baseUrl;
    private String chatCompletionsUrl;
    private String apiKey;
    private String model;
    private Integer timeoutSeconds;
}
