package com.bocoo.ai.service;

import com.bocoo.ai.config.AiAssistantProperties;
import com.bocoo.common.core.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@RequiredArgsConstructor
@Service
public class AiServiceSignatureService {

    private final AiAssistantProperties properties;
    private final AiServiceCredentialService credentialService;

    public void verify(HttpServletRequest request, String body) {
        String serviceName = header(request, "X-AI-Service");
        String keyVersion = header(request, "X-AI-Key-Version");
        String timestamp = header(request, "X-AI-Timestamp");
        String nonce = header(request, "X-AI-Nonce");
        String bodySha256 = header(request, "X-AI-Body-SHA256");
        String signature = header(request, "X-AI-Signature");

        long requestTime = parseTimestamp(timestamp);
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - requestTime) > properties.getSecurity().getServiceSignatureTtlSeconds()) {
            throw new ServiceException("AI service signature expired", 401);
        }
        String actualBodySha256 = sha256(body == null ? "" : body);
        if (!actualBodySha256.equals(bodySha256)) {
            throw new ServiceException("AI service body hash mismatch", 401);
        }
        String secret = credentialService.findActiveSecret(serviceName, keyVersion);
        String canonical = request.getMethod() + "\n" + request.getRequestURI() + "\n" + timestamp + "\n" + nonce + "\n" + bodySha256;
        String expected = hmac(secret, canonical);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new ServiceException("AI service signature mismatch", 401);
        }
        credentialService.markUsed(serviceName, keyVersion);
    }

    public String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new ServiceException("Failed to hash AI service request");
        }
    }

    private String hmac(String secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ServiceException("Failed to verify AI service signature");
        }
    }

    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            throw new ServiceException("Missing AI service header: " + name, 401);
        }
        return value.trim();
    }

    private long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (Exception e) {
            throw new ServiceException("Invalid AI service timestamp", 401);
        }
    }
}
