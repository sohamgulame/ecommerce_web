package com.Project1.project.service.impl;

import com.Project1.project.entity.OtpType;
import com.Project1.project.entity.OtpVerification;
import com.Project1.project.exception.InvalidOtpException;
import com.Project1.project.exception.OtpExpiredException;
import com.Project1.project.repository.OtpVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(otpVerificationRepository, 15L);
    }

    @Test
    void generateAndSaveOtp_generates6DigitCodeAndSavesHash() {
        when(otpVerificationRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArgument(0));

        String otp = otpService.generateAndSaveOtp("user@example.com", OtpType.EMAIL_VERIFICATION);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("^[0-9]{6}$"));

        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(captor.capture());

        OtpVerification saved = captor.getValue();
        assertEquals("user@example.com", saved.getEmail());
        assertEquals(OtpType.EMAIL_VERIFICATION, saved.getType());
        assertFalse(saved.isUsed());
        assertNotNull(saved.getOtpHash());
        assertNotEquals(otp, saved.getOtpHash()); // Hashed, not plain text
    }

    @Test
    void validateOtp_succeedsWithMatchingHash() {
        when(otpVerificationRepository.save(any(OtpVerification.class))).thenAnswer(i -> i.getArgument(0));
        String generatedOtp = otpService.generateAndSaveOtp("customer@test.com", OtpType.EMAIL_VERIFICATION);

        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(captor.capture());

        OtpVerification generatedVerification = captor.getValue();
        when(otpVerificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("customer@test.com", OtpType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(generatedVerification));

        boolean valid = otpService.validateOtp("customer@test.com", OtpType.EMAIL_VERIFICATION, generatedOtp);
        assertTrue(valid);
    }

    @Test
    void validateOtp_rejectsExpiredOtp() {
        OtpVerification expired = new OtpVerification();
        expired.setEmail("user@example.com");
        expired.setExpiresAt(Instant.now().minusSeconds(10));
        expired.setUsed(false);

        when(otpVerificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("user@example.com", OtpType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(expired));

        assertThrows(OtpExpiredException.class, () ->
                otpService.validateOtp("user@example.com", OtpType.EMAIL_VERIFICATION, "123456"));
    }

    @Test
    void validateOtp_rejectsInvalidCode() {
        String generatedOtp = otpService.generateAndSaveOtp("user@example.com", OtpType.PASSWORD_RESET);
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(captor.capture());

        when(otpVerificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("user@example.com", OtpType.PASSWORD_RESET))
                .thenReturn(Optional.of(captor.getValue()));

        assertThrows(InvalidOtpException.class, () ->
                otpService.validateOtp("user@example.com", OtpType.PASSWORD_RESET, "000000"));
    }

    @Test
    void markOtpAsUsed_updatesUsedFlag() {
        String generatedOtp = otpService.generateAndSaveOtp("user@example.com", OtpType.EMAIL_VERIFICATION);
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpVerificationRepository).save(captor.capture());

        OtpVerification verification = captor.getValue();
        when(otpVerificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc("user@example.com", OtpType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(verification));

        otpService.markOtpAsUsed("user@example.com", OtpType.EMAIL_VERIFICATION, generatedOtp);

        assertTrue(verification.isUsed());
        verify(otpVerificationRepository, times(2)).save(verification);
    }
}
