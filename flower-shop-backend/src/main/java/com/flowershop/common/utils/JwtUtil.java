package com.flowershop.common.utils;

import com.flowershop.common.config.FlowerProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final FlowerProperties flowerProperties;
    private final SecretKey secretKey;

    public JwtUtil(FlowerProperties flowerProperties) {
        this.flowerProperties = flowerProperties;
        this.secretKey = Keys.hmacShaKeyFor(flowerProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        Instant expireTime = now.plusSeconds(flowerProperties.getJwt().getExpireSeconds());
        return Jwts.builder()
            .issuer(flowerProperties.getJwt().getIssuer())
            .subject(username)
            .claim("userId", userId)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expireTime))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public long getExpireSeconds() {
        return flowerProperties.getJwt().getExpireSeconds();
    }
}
