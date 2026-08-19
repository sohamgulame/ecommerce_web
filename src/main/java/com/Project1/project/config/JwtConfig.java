package com.Project1.project.config;

import com.Project1.project.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret:}") String secret, @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured. Set the environment variable JWT_SECRET (or property jwt.secret) with a strong secret. Example (Linux/macOS): export JWT_SECRET='your-secret'\nWindows PowerShell: $env:JWT_SECRET='your-secret'. Also set JWT_EXPIRATION_MS if you wish to override the default expiration (milliseconds).");
        }
        return new JwtUtil(secret, expirationMs);
    }
}
