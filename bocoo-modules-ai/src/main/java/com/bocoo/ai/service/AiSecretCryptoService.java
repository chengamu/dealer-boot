package com.bocoo.ai.service;

import com.bocoo.ai.config.AiAssistantProperties;
import com.bocoo.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@RequiredArgsConstructor
@Service
public class AiSecretCryptoService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private final AiAssistantProperties properties;

    public String generateSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "bocoo_ai_sk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String generateVersion() {
        byte[] bytes = new byte[12];
        SECURE_RANDOM.nextBytes(bytes);
        return "v" + HexFormat.of().formatHex(bytes);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            throw ServiceException.ofMessageKey("ai.secret.blank");
        }
        try {
            byte[] iv = new byte[12];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return "v1:" + BASE64_ENCODER.encodeToString(iv) + ":" + BASE64_ENCODER.encodeToString(ciphertext);
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("ai.secret.encrypt.failed");
        }
    }

    public String decrypt(String value) {
        try {
            String[] parts = value == null ? new String[0] : value.split(":");
            if (parts.length != 3 || !"v1".equals(parts[0])) {
                throw ServiceException.ofMessageKey("ai.secret.ciphertext.invalid");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, BASE64_DECODER.decode(parts[1])));
            return new String(cipher.doFinal(BASE64_DECODER.decode(parts[2])), StandardCharsets.UTF_8);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("ai.secret.decrypt.failed");
        }
    }

    public String fingerprint(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("ai.secret.fingerprint.failed");
        }
    }

    private SecretKeySpec key() {
        String raw = properties.getSecurity().getSecretEncryptionKey();
        if (raw == null || raw.isBlank()) {
            throw ServiceException.ofMessageKey("ai.secret.encryptionKey.missing");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("ai.secret.encryptionKey.invalid");
        }
    }
}
