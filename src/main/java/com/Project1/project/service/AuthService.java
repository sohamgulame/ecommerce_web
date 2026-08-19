package com.Project1.project.service;

import com.Project1.project.dto.request.*;
import com.Project1.project.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO refresh(RefreshTokenRequestDTO request);
    void logout(RefreshTokenRequestDTO request);
    AuthResponseDTO verifyEmail(VerifyEmailRequestDTO request);
    void resendOtp(ResendOtpRequestDTO request);
    void forgotPassword(ForgotPasswordRequestDTO request);
    void resetPassword(ResetPasswordRequestDTO request);
}
