package com.marquify.beta.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import com.marquify.beta.entity.Cliente;
import com.marquify.beta.entity.Vendedor;
import com.marquify.beta.entity.ContaProfissional;
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
                .subject(subject(usuario))
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getSigningKey())
                .compact();
    }

    private String subject(UserDetails usuario) {
        if (usuario instanceof Cliente cliente && cliente.getId() != null) return "cliente:" + cliente.getId();
        if (usuario instanceof Vendedor vendedor && vendedor.getId() != null) return "vendedor:" + vendedor.getId();
        if (usuario instanceof ContaProfissional profissional && profissional.getId() != null) return "profissional:" + profissional.getId();
        throw new IllegalArgumentException("Identidade não suportada");
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
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
