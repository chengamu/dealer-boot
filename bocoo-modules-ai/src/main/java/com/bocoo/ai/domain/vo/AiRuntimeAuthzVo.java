package com.bocoo.ai.domain.vo;

import lombok.Data;

@Data
public class AiRuntimeAuthzVo {
    private boolean allowed;
    private String reason;
    private Long tenantId;
    private Long userId;
    private String username;
    private String providerCode;
    private String model;
}
