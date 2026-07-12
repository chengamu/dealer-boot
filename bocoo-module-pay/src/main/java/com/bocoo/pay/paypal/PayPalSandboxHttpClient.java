package com.bocoo.pay.paypal;

import com.bocoo.common.core.exception.ServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
class PayPalSandboxHttpClient {
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);
    private static final long TOKEN_EXPIRY_SKEW_SECONDS = 60;

    private final PayPalSandboxCredentials credentials;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private volatile AccessToken cachedToken;

    PayPalSandboxHttpClient(PayPalSandboxCredentials credentials, ObjectMapper objectMapper,
                           RestTemplateBuilder restTemplateBuilder) {
        this.credentials = credentials;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setReadTimeout(READ_TIMEOUT)
            .build();
    }

    JsonNode post(String path, Object body, String requestId) {
        long started = System.nanoTime();
        try {
            JsonNode result = restTemplate.postForEntity(PayPalSandboxCredentials.BASE_URL + path,
                new HttpEntity<>(body, headers(requestId)), JsonNode.class).getBody();
            logSuccess("POST", path, requestId, started);
            return result;
        } catch (RestClientException ex) {
            logFailure("POST", path, requestId, started, ex);
            throw new ServiceException("PayPal Sandbox request failed");
        }
    }

    JsonNode get(String path) {
        long started = System.nanoTime();
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(PayPalSandboxCredentials.BASE_URL + path,
                org.springframework.http.HttpMethod.GET, new HttpEntity<>(headers(null)), JsonNode.class);
            logSuccess("GET", path, null, started);
            return response.getBody();
        } catch (RestClientException ex) {
            logFailure("GET", path, null, started, ex);
            throw new ServiceException("PayPal Sandbox order query failed");
        }
    }

    JsonNode readTree(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (JsonProcessingException ex) {
            throw new ServiceException("Invalid PayPal webhook body");
        }
    }

    private HttpHeaders headers(String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");
        if (requestId != null) headers.set("PayPal-Request-Id", requestId);
        return headers;
    }

    private String accessToken() {
        AccessToken current = cachedToken;
        if (current != null && current.validAt(Instant.now())) return current.value();
        synchronized (this) {
            current = cachedToken;
            if (current != null && current.validAt(Instant.now())) return current.value();
            cachedToken = requestAccessToken();
            return cachedToken.value();
        }
    }

    private AccessToken requestAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(credentials.clientId(), credentials.clientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        long started = System.nanoTime();
        try {
            JsonNode result = restTemplate.postForObject(PayPalSandboxCredentials.BASE_URL + "/v1/oauth2/token",
                new HttpEntity<>(form, headers), JsonNode.class);
            if (result == null || result.path("access_token").asText().isBlank()) {
                throw new ServiceException("PayPal Sandbox returned an empty access token");
            }
            long expiresIn = result.path("expires_in").asLong(300);
            Instant expiresAt = Instant.now().plusSeconds(Math.max(1, expiresIn - TOKEN_EXPIRY_SKEW_SECONDS));
            logSuccess("POST", "/v1/oauth2/token", null, started);
            return new AccessToken(result.path("access_token").asText(), expiresAt);
        } catch (RestClientException ex) {
            logFailure("POST", "/v1/oauth2/token", null, started, ex);
            throw new ServiceException("PayPal Sandbox authentication failed");
        }
    }

    private void logSuccess(String method, String path, String requestId, long started) {
        log.info("PayPal call completed: method={}, path={}, requestId={}, durationMs={}",
            method, path, requestId, elapsedMillis(started));
    }

    private void logFailure(String method, String path, String requestId, long started, Exception error) {
        log.warn("PayPal call failed: method={}, path={}, requestId={}, durationMs={}, errorType={}",
            method, path, requestId, elapsedMillis(started), error.getClass().getSimpleName());
    }

    private long elapsedMillis(long started) {
        return Duration.ofNanos(System.nanoTime() - started).toMillis();
    }

    private record AccessToken(String value, Instant expiresAt) {
        boolean validAt(Instant now) {
            return expiresAt.isAfter(now);
        }
    }
}
