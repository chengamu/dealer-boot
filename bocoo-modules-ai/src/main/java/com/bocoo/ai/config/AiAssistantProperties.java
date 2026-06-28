package com.bocoo.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI assistant integration settings.
 */
@Data
@Component
@ConfigurationProperties(prefix = "bocoo.ai")
public class AiAssistantProperties {

    private boolean enabled;

    private String defaultModel = "deepseek-v4-flash";

    private String pageAgentBaseUrl = "/ai-runtime/page-agent";

    private Security security = new Security();

    @Data
    public static class Security {
        private String secretEncryptionKey = "";
        private String channelTokenSecret = "";
        private int channelTokenTtlSeconds = 900;
        private int serviceSignatureTtlSeconds = 300;
    }
}
