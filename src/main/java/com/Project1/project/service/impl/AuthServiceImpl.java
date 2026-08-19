package com.Project1.project.service.impl;

import com.Project1.project.dto.request.*;
import com.Project1.project.dto.response.AuthResponseDTO;
import com.Project1.project.entity.OtpType;
import com.Project1.project.entity.RefreshToken;
import com.Project1.project.entity.Role;
import com.Project1.project.entity.User;
import com.Project1.project.exception.EmailAlreadyExistsException;
import com.Project1.project.exception.InvalidRefreshTokenException;
import com.Project1.project.repository.RefreshTokenRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.security.JwtUtil;
import com.Project1.project.service.AuthService;
import com.Project1.project.service.EmailService;
import com.Project1.project.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final long refreshExpirationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           RefreshTokenRepository refreshTokenRepository,
                           OtpService otpService,
                           EmailService emailService,
                           @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail().trim().toLowerCase()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use");
        }
        User u = new User();
        u.setName(request.getName());
        u.setEmail(request.getEmail().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setPhone(request.getPhone());
        u.setAddress(request.getAddress());
        u.setRole(Role.ROLE_CUSTOMER);
        u.setEmailVerified(false);
        User savedUser = userRepository.save(u);

        // Generate and send verification OTP email
        String otp = otpService.generateAndSaveOtp(savedUser.getEmail(), OtpType.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(savedUser.getEmail(), otp);

        return createAuthResponse(savedUser);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
        return createAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponseDTO refresh(RefreshTokenRequestDTO request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        if (refreshToken.isRevoked() || !refreshToken.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidRefreshTokenException("Expired or revoked refresh token");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return createAuthResponse(refreshToken.getUser());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequestDTO request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hashToken(request.getRefreshToken()))
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Override
    @Transactional
    public AuthResponseDTO verifyEmail(VerifyEmailRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.Project1.project.exception.InvalidOtpException("User not found for given email"));

        otpService.validateOtp(email, OtpType.EMAIL_VERIFICATION, request.getOtp());
        otpService.markOtpAsUsed(email, OtpType.EMAIL_VERIFICATION, request.getOtp());

        user.setEmailVerified(true);
        User updatedUser = userRepository.save(user);

        return createAuthResponse(updatedUser);
    }

    @Override
    @Transactional
    public void resendOtp(ResendOtpRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = otpService.generateAndSaveOtp(email, request.getType());
            if (request.getType() == OtpType.EMAIL_VERIFICATION) {
                emailService.sendVerificationEmail(email, otp);
            } else if (request.getType() == OtpType.PASSWORD_RESET) {
                emailService.sendPasswordResetEmail(email, otp);
            }
        });
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            String otp = otpService.generateAndSaveOtp(email, OtpType.PASSWORD_RESET);
            emailService.sendPasswordResetEmail(email, otp);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.Project1.project.exception.InvalidOtpException("User not found for given email"));

        otpService.validateOtp(email, OtpType.PASSWORD_RESET, request.getOtp());
        otpService.markOtpAsUsed(email, OtpType.PASSWORD_RESET, request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private AuthResponseDTO createAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String rawRefreshToken = generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponseDTO(accessToken, rawRefreshToken);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
