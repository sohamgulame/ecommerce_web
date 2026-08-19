package com.Project1.project.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private final Algorithm algorithm;
    private final long expirationMs;

    public JwtUtil(String secret, long expirationMs) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        return generateToken(username, null);
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        var builder = JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(exp);
        if (role != null) {
            builder.withClaim("role", role);
        }
        return builder.sign(algorithm);
    }

    public String validateAndGetSubject(String token) {
        try {
            DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
            return decoded.getSubject();
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    public String validateAndGetRole(String token) {
        try {
            DecodedJWT decoded = JWT.require(algorithm).build().verify(token);
            return decoded.getClaim("role").asString();
        } catch (JWTVerificationException ex) {
            return null;
        }
    }
}
