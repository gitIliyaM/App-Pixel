package ru.pionerpixel.service;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.Date;

@Slf4j
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Секретный ключ JWT не настроен");
        }
        if (accessExpiration <= 0) {
            throw new IllegalStateException("Время жизни access-токена должно быть положительным");
        }
        if (refreshExpiration <= 0) {
            throw new IllegalStateException("Время жизни refresh-токена должно быть положительным");
        }
    }

    public String generateAccessToken(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null");
        }
        return buildToken(userId, accessExpiration);
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId, refreshExpiration);
    }

    private String buildToken(Long userId, long expiration) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    public Long extractUserId(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Токен не может быть null или пустым");
        }

        try {
            Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException ex) {
            throw ex; // Позволяет обработать отдельно
        } catch (Exception e) {
            log.error("Ошибка извлечения ID пользователя из токена", e);
            throw new JwtException("Неверный токен");
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Токен просрочен", ex);
            throw ex;
        } catch (Exception e) {
            log.error("Неверный токен", e);
            return false;
        }
    }
}