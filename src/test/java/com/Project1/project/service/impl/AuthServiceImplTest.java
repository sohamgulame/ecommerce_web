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
import com.Project1.project.service.EmailService;
import com.Project1.project.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private OtpService otpService;
    @Mock private EmailService emailService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager, jwtUtil,
                refreshTokenRepository, otpService, emailService, 604800000L);
    }

    @Test
    void register_createsUserAndDispatchesVerificationEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("John Doe");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(otpService.generateAndSaveOtp("newuser@example.com", OtpType.EMAIL_VERIFICATION)).thenReturn("123456");
        when(jwtUtil.generateToken("newuser@example.com", "ROLE_CUSTOMER")).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        verify(emailService).sendVerificationEmail("newuser@example.com", "123456");
    }

    @Test
    void register_throwsConflictWhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user()));

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void verifyEmail_validatesOtpAndMarksEmailVerified() {
        User user = user();
        user.setEmailVerified(false);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken("customer@example.com", "ROLE_CUSTOMER")).thenReturn("verified-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        VerifyEmailRequestDTO request = new VerifyEmailRequestDTO("customer@example.com", "123456");
        AuthResponseDTO response = authService.verifyEmail(request);

        assertNotNull(response);
        assertTrue(user.isEmailVerified());
        verify(otpService).validateOtp("customer@example.com", OtpType.EMAIL_VERIFICATION, "123456");
        verify(otpService).markOtpAsUsed("customer@example.com", OtpType.EMAIL_VERIFICATION, "123456");
        verify(userRepository).save(user);
    }

    @Test
    void forgotPassword_generatesResetOtpWhenUserExists() {
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user()));
        when(otpService.generateAndSaveOtp("customer@example.com", OtpType.PASSWORD_RESET)).thenReturn("999888");

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("customer@example.com");
        authService.forgotPassword(request);

        verify(otpService).generateAndSaveOtp("customer@example.com", OtpType.PASSWORD_RESET);
        verify(emailService).sendPasswordResetEmail("customer@example.com", "999888");
    }

    @Test
    void resetPassword_updatesPasswordUponValidOtp() {
        User user = user();
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new-secret");

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("customer@example.com", "999888", "new-secret");
        authService.resetPassword(request);

        verify(otpService).validateOtp("customer@example.com", OtpType.PASSWORD_RESET, "999888");
        verify(otpService).markOtpAsUsed("customer@example.com", OtpType.PASSWORD_RESET, "999888");
        assertEquals("encoded-new-secret", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void refresh_rotatesActiveTokenAndReturnsNewTokenPair() {
        User user = user();
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setExpiresAt(Instant.now().plusSeconds(60));
        storedToken.setRevoked(false);

        when(jwtUtil.generateToken("customer@example.com", "ROLE_CUSTOMER")).thenReturn("access-one", "access-two");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponseDTO loginResponse = authService.login(loginRequest());
        assertNotNull(loginResponse.getRefreshToken());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO();
        refreshRequest.setRefreshToken(loginResponse.getRefreshToken());

        AuthResponseDTO refreshResponse = authService.refresh(refreshRequest);

        assertEquals("access-two", refreshResponse.getAccessToken());
        assertNotEquals(loginResponse.getRefreshToken(), refreshResponse.getRefreshToken());
        assertTrue(storedToken.isRevoked());
        verify(refreshTokenRepository, times(3)).save(any(RefreshToken.class));
    }

    @Test
    void logout_revokesRefreshTokenWhenPresent() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setRevoked(false);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("some-refresh-token");

        authService.logout(request);

        assertTrue(storedToken.isRevoked());
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void logout_handlesNullOrEmptyTokenGracefully() {
        authService.logout(null);
        RefreshTokenRequestDTO emptyRequest = new RefreshTokenRequestDTO();
        authService.logout(emptyRequest);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_rejectsExpiredToken() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setExpiresAt(Instant.now().minusSeconds(1));
        expiredToken.setRevoked(false);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
        request.setRefreshToken("expired-token");

        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));
        verify(refreshTokenRepository, never()).save(any());
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("customer@example.com");
        request.setPassword("password");
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(user()));
        return request;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("customer@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_CUSTOMER);
        return user;
    }
}
