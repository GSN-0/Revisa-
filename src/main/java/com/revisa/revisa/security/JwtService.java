package com.revisa.revisa.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.issuer:revisa-api}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(secret) || secret.getBytes().length < 32) {
            throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 caracteres");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String gerarToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .issuer(issuer)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String validarToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .clockSkewSeconds(30)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
