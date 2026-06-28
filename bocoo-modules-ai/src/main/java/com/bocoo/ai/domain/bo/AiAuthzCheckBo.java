package com.bocoo.ai.domain.bo;

import lombok.Data;

@Data
public class AiAuthzCheckBo {
    private String channel;
    private String routePath;
    private String requestId;
    private String sessionId;
    private String actionType;
    private String toolCode;
    private String channelToken;
}
