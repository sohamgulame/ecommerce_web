package com.Project1.project.controller;

import com.Project1.project.dto.request.*;
import com.Project1.project.dto.response.AuthResponseDTO;
import com.Project1.project.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req, HttpServletResponse response) {
        Object result = authService.register(req);
        if (result instanceof AuthResponseDTO authRes) {
            setRefreshTokenCookie(response, authRes.getRefreshToken());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req, HttpServletResponse response) {
        AuthResponseDTO result = authService.login(req);
        setRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody(required = false) RefreshTokenRequestDTO req,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String token = (req != null && req.getRefreshToken() != null && !req.getRefreshToken().isBlank())
                ? req.getRefreshToken()
                : extractRefreshTokenFromCookies(request);

        if (token == null || token.isBlank()) {
            throw new com.Project1.project.exception.InvalidRefreshTokenException("Missing refresh token in request body or cookie");
        }

        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken(token);
        AuthResponseDTO result = authService.refresh(dto);
        setRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequestDTO req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String token = (req != null && req.getRefreshToken() != null && !req.getRefreshToken().isBlank())
                ? req.getRefreshToken()
                : extractRefreshTokenFromCookies(request);

        if (token != null && !token.isBlank()) {
            RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
            dto.setRefreshToken(token);
            authService.logout(dto);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO req, HttpServletResponse response) {
        AuthResponseDTO result = authService.verifyEmail(req);
        setRefreshTokenCookie(response, result.getRefreshToken());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequestDTO req) {
        authService.resendOtp(req);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a password reset OTP has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You may now login with your new password."));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // Set to true if behind HTTPS in production
                    .sameSite("Strict")
                    .path("/api/v1/auth")
                    .maxAge(Duration.ofDays(7))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

