package com.bocoo.ai.domain.vo;

import lombok.Data;

@Data
public class AiServiceCredentialSecretVo {
    private AiServiceCredentialVo credential;
    private String secret;
}
