package com.marquify.beta.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Service
public class TokenService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public TokenService(TokenProperties properties) {
        properties.validate();
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = properties.getExpiration().toMillis();
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public String gerarToken(UserDetails usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMillis);

        return Jwts.builder()
                .issuer("marquify-api")
                .subject(usuario.getUsername())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getSigningKey())
                .compact();
    }

    public String validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer("marquify-api")
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (ExpiredJwtException | io.jsonwebtoken.security.SignatureException |
                 io.jsonwebtoken.MalformedJwtException e) {
            return null;
        }
    }
}
