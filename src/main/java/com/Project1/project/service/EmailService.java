package com.Project1.project.service;

import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.OrderStatus;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String otp);
    void sendPasswordResetEmail(String toEmail, String otp);
    void sendOrderConfirmationEmail(String toEmail, OrderResponseDTO order);
    void sendOrderStatusUpdateEmail(String toEmail, Long orderId, OrderStatus status);
}
