package com.marquify.beta.infra.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTests {
    private static final String TEST_SECRET = "marquify-test-only-key-never-use-in-production";

    @Test
    void rejectsMissingOrWeakSecretAtStartup() {
        TokenProperties properties = new TokenProperties();
        assertThatThrownBy(() -> new TokenService(properties)).isInstanceOf(IllegalArgumentException.class);
        properties.setSecret("short");
        assertThatThrownBy(() -> new TokenService(properties)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidExpirationAtStartup() {
        TokenProperties properties = properties();
        for (Duration duration : new Duration[] {Duration.ZERO, Duration.ofSeconds(-1), Duration.ofDays(31)}) {
            properties.setExpiration(duration);
            assertThatThrownBy(() -> new TokenService(properties)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void issuesVerifiableTokenWithConfiguredExpiration() {
        TokenProperties properties = properties();
        properties.setExpiration(Duration.ofMinutes(15));
        TokenService service = new TokenService(properties);
        String token = service.gerarToken(User.withUsername("cliente@example.test")
                .password("unused").roles("USER").build());
        var claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
        assertThat(service.validarToken(token)).isEqualTo("cliente@example.test");
        assertThat(claims.getIssuer()).isEqualTo("marquify-api");
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(900_000);
    }

    private TokenProperties properties() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(TEST_SECRET);
        return properties;
    }
}
