package com.Project1.project.service.impl;

import com.Project1.project.entity.OtpType;
import com.Project1.project.entity.OtpVerification;
import com.Project1.project.exception.InvalidOtpException;
import com.Project1.project.exception.OtpExpiredException;
import com.Project1.project.repository.OtpVerificationRepository;
import com.Project1.project.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final long expirationMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(OtpVerificationRepository otpVerificationRepository,
                          @Value("${app.otp.expiration-minutes:15}") long expirationMinutes) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generateAndSaveOtp(String email, OtpType type) {
        int code = secureRandom.nextInt(1_000_000);
        String rawOtp = String.format("%06d", code);
        String hash = hashOtp(rawOtp);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes));

        OtpVerification verification = new OtpVerification(email.trim().toLowerCase(), hash, type, expiresAt);
        otpVerificationRepository.save(verification);
        return rawOtp;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateOtp(String email, OtpType type, String rawOtp) {
        OtpVerification verification = otpVerificationRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email.trim().toLowerCase(), type)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP code"));

        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new OtpExpiredException("OTP code has expired. Please request a new one.");
        }

        String inputHash = hashOtp(rawOtp.trim());
        if (!verification.getOtpHash().equals(inputHash)) {
            throw new InvalidOtpException("Invalid OTP code");
        }

        return true;
    }

    @Override
    public void markOtpAsUsed(String email, OtpType type, String rawOtp) {
        OtpVerification verification = otpVerificationRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email.trim().toLowerCase(), type)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP code"));

        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new OtpExpiredException("OTP code has expired. Please request a new one.");
        }

        String inputHash = hashOtp(rawOtp.trim());
        if (!verification.getOtpHash().equals(inputHash)) {
            throw new InvalidOtpException("Invalid OTP code");
        }

        verification.setUsed(true);
        otpVerificationRepository.save(verification);
    }

    private String hashOtp(String otp) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
