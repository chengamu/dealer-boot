package com.bocoo.system.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class GoogleAuthService {

    private static final String DEFAULT_GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_ISSUER = "accounts.google.com";
    private static final String GOOGLE_ISSUER_HTTPS = "https://accounts.google.com";
    private static final String GOOGLE_ALGORITHM = "RS256";
    private static final Duration GOOGLE_KEY_CACHE_TTL = Duration.ofHours(6);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private volatile Map<String, RSAPublicKey> googlePublicKeys = Map.of();
    private volatile Instant googlePublicKeysExpireAt = Instant.EPOCH;

    @Value("${auth.google.client-id:}")
    private String googleClientId;

    @Value("${auth.google.allow-unhosted-email:false}")
    private Boolean googleAllowUnhostedEmail;

    @Value("${auth.google.certs-url:" + DEFAULT_GOOGLE_CERTS_URL + "}")
    private String googleCertsUrl;

    public String verifyCredential(String credential) {
        if (StringUtils.isBlank(googleClientId)) {
            throw ServiceException.ofMessageKey("auth.google.notConfigured");
        }
        try {
            String[] segments = credential.split("\\.");
            if (segments.length != 3) {
                throw ServiceException.ofMessageKey("auth.google.invalidToken");
            }
            JsonNode header = decodeJwtJson(segments[0]);
            JsonNode payload = decodeJwtJson(segments[1]);
            String algorithm = header.path("alg").asText();
            String keyId = header.path("kid").asText();
            if (!GOOGLE_ALGORITHM.equals(algorithm) || StringUtils.isBlank(keyId)) {
                throw ServiceException.ofMessageKey("auth.google.invalidToken");
            }
            RSAPublicKey publicKey = getGooglePublicKey(keyId);
            verifyGoogleSignature(publicKey, segments);
            validateGoogleClaims(payload);
            String email = payload.path("email").asText("").trim().toLowerCase(Locale.ROOT);
            boolean emailVerified = payload.path("email_verified").asBoolean(false);
            String hostedDomain = payload.path("hd").asText("");
            if (StringUtils.isBlank(email) || !emailVerified || !isTrustedGoogleEmail(email, hostedDomain)) {
                throw ServiceException.ofMessageKey("auth.google.emailUntrusted");
            }
            return email;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Google login token verification failed: {}", ex.getMessage());
            throw ServiceException.ofMessageKey("auth.google.invalidToken");
        }
    }

    private JsonNode decodeJwtJson(String segment) throws Exception {
        return OBJECT_MAPPER.readTree(Base64.getUrlDecoder().decode(segment));
    }

    private void verifyGoogleSignature(RSAPublicKey publicKey, String[] segments) throws Exception {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update((segments[0] + "." + segments[1]).getBytes(StandardCharsets.US_ASCII));
        if (!verifier.verify(Base64.getUrlDecoder().decode(segments[2]))) {
            throw ServiceException.ofMessageKey("auth.google.invalidToken");
        }
    }

    private void validateGoogleClaims(JsonNode payload) {
        String audience = payload.path("aud").asText();
        String issuer = payload.path("iss").asText();
        long expiresAt = payload.path("exp").asLong(0);
        if (!googleClientId.equals(audience)) {
            throw ServiceException.ofMessageKey("auth.google.invalidAudience");
        }
        if (!GOOGLE_ISSUER.equals(issuer) && !GOOGLE_ISSUER_HTTPS.equals(issuer)) {
            throw ServiceException.ofMessageKey("auth.google.invalidToken");
        }
        if (expiresAt <= Instant.now().getEpochSecond()) {
            throw ServiceException.ofMessageKey("auth.google.expiredToken");
        }
    }

    private boolean isTrustedGoogleEmail(String email, String hostedDomain) {
        if (email.endsWith("@gmail.com") || StringUtils.isNotBlank(hostedDomain)) {
            return true;
        }
        return Boolean.TRUE.equals(googleAllowUnhostedEmail);
    }

    private RSAPublicKey getGooglePublicKey(String keyId) throws Exception {
        Map<String, RSAPublicKey> keys = googlePublicKeys;
        if (keys.containsKey(keyId) && Instant.now().isBefore(googlePublicKeysExpireAt)) {
            return keys.get(keyId);
        }
        synchronized (this) {
            keys = googlePublicKeys;
            if (keys.containsKey(keyId) && Instant.now().isBefore(googlePublicKeysExpireAt)) {
                return keys.get(keyId);
            }
            googlePublicKeys = fetchGooglePublicKeys();
            googlePublicKeysExpireAt = Instant.now().plus(GOOGLE_KEY_CACHE_TTL);
            RSAPublicKey publicKey = googlePublicKeys.get(keyId);
            if (publicKey == null) {
                throw ServiceException.ofMessageKey("auth.google.invalidToken");
            }
            return publicKey;
        }
    }

    private Map<String, RSAPublicKey> fetchGooglePublicKeys() throws Exception {
        String certsUrl = StringUtils.isBlank(googleCertsUrl) ? DEFAULT_GOOGLE_CERTS_URL : googleCertsUrl;
        HttpRequest request = HttpRequest.newBuilder(URI.create(certsUrl))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw ServiceException.ofMessageKey("auth.google.keyFetchFailed");
        }
        JsonNode keys = OBJECT_MAPPER.readTree(response.body()).path("keys");
        Map<String, RSAPublicKey> publicKeys = new HashMap<>();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        for (JsonNode key : keys) {
            String kid = key.path("kid").asText();
            String n = key.path("n").asText();
            String e = key.path("e").asText();
            if (StringUtils.isBlank(kid) || StringUtils.isBlank(n) || StringUtils.isBlank(e)) {
                continue;
            }
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
            publicKeys.put(kid, (RSAPublicKey) keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent)));
        }
        if (publicKeys.isEmpty()) {
            throw ServiceException.ofMessageKey("auth.google.keyFetchFailed");
        }
        return publicKeys;
    }
}
