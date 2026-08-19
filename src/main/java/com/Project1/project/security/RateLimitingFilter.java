package com.Project1.project.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance, IP-based sliding window rate limiter filter for sensitive authentication endpoints
 * to protect against credential stuffing, brute-force password guessing, and registration flooding.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final long WINDOW_MILLIS = 60_000L; // 1 minute window

    private final Map<String, RequestBucket> ipBuckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Apply rate limiting specifically to POST /api/v1/auth/** endpoints
        if ("POST".equalsIgnoreCase(method) && path.startsWith("/api/v1/auth/")) {
            String clientIp = getClientIp(request);
            long currentTime = System.currentTimeMillis();

            RequestBucket bucket = ipBuckets.compute(clientIp, (key, existing) -> {
                if (existing == null || (currentTime - existing.windowStart) > WINDOW_MILLIS) {
                    return new RequestBucket(currentTime, new AtomicInteger(1));
                }
                existing.counter.incrementAndGet();
                return existing;
            });

            if (bucket.counter.get() > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP '{}' on path '{}' (Attempt: {})", clientIp, path, bucket.counter.get());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", "60");

                Map<String, Object> errorBody = Map.of(
                        "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                        "message", "Too many requests. Please wait a minute before trying again.",
                        "timestamp", Instant.now().toString(),
                        "path", path
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorBody));
                return;
            }
        }

        // Cleanup stale entries occasionally if map grows
        if (ipBuckets.size() > 5000) {
            long now = System.currentTimeMillis();
            ipBuckets.entrySet().removeIf(entry -> (now - entry.getValue().windowStart) > WINDOW_MILLIS);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestBucket {
        final long windowStart;
        final AtomicInteger counter;

        RequestBucket(long windowStart, AtomicInteger counter) {
            this.windowStart = windowStart;
            this.counter = counter;
        }
    }
}
