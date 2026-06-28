package com.bocoo.ai.domain.bo;

import lombok.Data;

import java.util.Map;

@Data
public class AiToolExecuteBo {
    private String sessionId;
    private String requestId;
    private String toolCode;
    private String action;
    private Map<String, Object> params;
}
