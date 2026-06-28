package com.bocoo.ai.service;

import com.bocoo.ai.config.AiAssistantProperties;
import cn.dev33.satoken.stp.StpUtil;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AiRuntimeTokenService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AiAssistantProperties properties;
    private final ObjectMapper objectMapper;

    public String issueChannelToken() {
        Map<String, Object> claims = commonClaims("page-agent-channel", properties.getSecurity().getChannelTokenTtlSeconds());
        claims.put("admin", LoginHelper.isAdmin());
        claims.put("permissions", safePermissions());
        return sign(claims);
    }

    public Map<String, Object> verifyChannelToken(String token) {
        return verify(token, "page-agent-channel");
    }

    public Map<String, Object> verify(String token, String audience) {
        try {
            String normalized = token == null ? "" : token.replaceFirst("(?i)^Bearer\\s+", "");
            String[] parts = normalized.split("\\.");
            if (parts.length != 3) {
                throw new ServiceException("Invalid AI internal token", 401);
            }
            String signingInput = parts[0] + "." + parts[1];
            String expected = hmac(signingInput);
            if (!expected.equals(parts[2])) {
                throw new ServiceException("Invalid AI internal token signature", 401);
            }
            Map<String, Object> claims = objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), MAP_TYPE);
            long exp = Long.parseLong(String.valueOf(claims.getOrDefault("exp", 0)));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new ServiceException("AI internal token expired", 401);
            }
            if (!audience.equals(claims.get("aud"))) {
                throw new ServiceException("AI internal token audience mismatch", 401);
            }
            return claims;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Invalid AI internal token", 401);
        }
    }

    private Map<String, Object> commonClaims(String audience, long ttlSeconds) {
        Map<String, Object> claims = new LinkedHashMap<>();
        long now = Instant.now().getEpochSecond();
        claims.put("iss", "dealer-admin");
        claims.put("aud", audience);
        claims.put("iat", now);
        claims.put("exp", now + ttlSeconds);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("tenantId", LoginHelper.getTenantId());
        claims.put("userId", LoginHelper.getUserId());
        claims.put("username", LoginHelper.getUsername());
        return claims;
    }

    private String sign(Map<String, Object> claims) {
        try {
            String header = BASE64_URL_ENCODER.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            String payload = BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(claims));
            return header + "." + payload + "." + hmac(header + "." + payload);
        } catch (Exception e) {
            throw new ServiceException("Failed to issue AI internal token");
        }
    }

    private String hmac(String value) throws Exception {
        String secret = properties.getSecurity().getChannelTokenSecret();
        if (secret == null || secret.isBlank()) {
            secret = properties.getSecurity().getSecretEncryptionKey();
        }
        if (secret == null || secret.isBlank()) {
            throw new ServiceException("AI channel token secret is not configured");
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private List<String> safePermissions() {
        try {
            return StpUtil.getPermissionList();
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
