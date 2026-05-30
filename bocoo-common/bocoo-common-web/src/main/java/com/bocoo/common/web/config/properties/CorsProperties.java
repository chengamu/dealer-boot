package com.bocoo.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "web.cors")
public class CorsProperties {

    private Boolean allowCredentials = true;

    private List<String> allowedOrigins = new ArrayList<>();

    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8080",
        "http://localhost:8081",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:8080",
        "http://127.0.0.1:8081"
    ));

    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    private Long maxAge = 1800L;
}
