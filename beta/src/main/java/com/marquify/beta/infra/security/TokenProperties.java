package com.marquify.beta.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "api.security.token")
public class TokenProperties {
    private String secret;
    private Duration expiration = Duration.ofHours(2);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

    public void validate() {
        if (secret == null || secret.isBlank() || secret.startsWith("${")
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve conter pelo menos 32 bytes UTF-8.");
        }
        if (expiration == null || expiration.compareTo(Duration.ofSeconds(1)) < 0
                || expiration.compareTo(Duration.ofDays(30)) > 0) {
            throw new IllegalArgumentException("JWT_EXPIRATION deve estar entre 1s e 30d.");
        }
    }
}
