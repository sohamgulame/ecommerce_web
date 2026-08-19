package com.Project1.project.service;

import com.Project1.project.entity.OtpType;

public interface OtpService {
    String generateAndSaveOtp(String email, OtpType type);
    boolean validateOtp(String email, OtpType type, String rawOtp);
    void markOtpAsUsed(String email, OtpType type, String rawOtp);
}
