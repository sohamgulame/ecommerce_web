package com.Project1.project.service.impl;

import com.Project1.project.dto.response.OrderItemResponseDTO;
import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.OrderStatus;
import com.Project1.project.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceImpl(@org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
                            @Value("${app.mail.from:noreply@ecommerce.local}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendVerificationEmail(String toEmail, String otp) {
        String subject = "Verify Your E-Commerce Account";
        String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <h2 style="color: #4f46e5; margin-top: 0;">Welcome to E-Commerce Store!</h2>
                    <p style="color: #475569; font-size: 16px;">Thank you for registering. Please use the following 6-digit One-Time Password (OTP) to verify your email address:</p>
                    <div style="background-color: #f1f5f9; padding: 18px; border-radius: 8px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1e293b;">%s</span>
                    </div>
                    <p style="color: #64748b; font-size: 14px;">This code is valid for <strong>15 minutes</strong>. If you did not create an account, please disregard this message.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">© E-Commerce Inc. All rights reserved.</p>
                </div>
                """.formatted(otp);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendPasswordResetEmail(String toEmail, String otp) {
        String subject = "Reset Your E-Commerce Password";
        String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <h2 style="color: #dc2626; margin-top: 0;">Password Reset Request</h2>
                    <p style="color: #475569; font-size: 16px;">We received a request to reset your password. Use the following 6-digit OTP code to complete the process:</p>
                    <div style="background-color: #fef2f2; padding: 18px; border-radius: 8px; text-align: center; margin: 24px 0; border: 1px solid #fee2e2;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #991b1b;">%s</span>
                    </div>
                    <p style="color: #64748b; font-size: 14px;">This code expires in <strong>15 minutes</strong>. If you did not request a password reset, your account is safe and you can ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">© E-Commerce Inc. All rights reserved.</p>
                </div>
                """.formatted(otp);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendOrderConfirmationEmail(String toEmail, OrderResponseDTO order) {
        String subject = "Order Confirmation #" + order.getId() + " - E-Commerce Store";

        StringBuilder itemsTable = new StringBuilder();
        itemsTable.append("<table style=\"width: 100%; border-collapse: collapse; margin-top: 16px;\">");
        itemsTable.append("<tr style=\"background-color: #f8fafc; border-bottom: 2px solid #e2e8f0; text-align: left;\">");
        itemsTable.append("<th style=\"padding: 10px;\">Item</th><th style=\"padding: 10px;\">Qty</th><th style=\"padding: 10px;\">Price</th><th style=\"padding: 10px;\">Subtotal</th>");
        itemsTable.append("</tr>");

        if (order.getItems() != null) {
            for (OrderItemResponseDTO item : order.getItems()) {
                itemsTable.append("<tr style=\"border-bottom: 1px solid #e2e8f0;\">");
                itemsTable.append("<td style=\"padding: 10px; color: #1e293b;\">").append(item.getProductName()).append("</td>");
                itemsTable.append("<td style=\"padding: 10px; color: #475569;\">").append(item.getQuantity()).append("</td>");
                itemsTable.append("<td style=\"padding: 10px; color: #475569;\">$").append(item.getPrice()).append("</td>");
                itemsTable.append("<td style=\"padding: 10px; font-weight: 600; color: #1e293b;\">$").append(item.getSubtotal()).append("</td>");
                itemsTable.append("</tr>");
            }
        }
        itemsTable.append("</table>");

        String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <h2 style="color: #059669; margin-top: 0;">Thank you for your order!</h2>
                    <p style="color: #475569; font-size: 15px;">Your order <strong>#%d</strong> has been successfully placed.</p>
                    <div style="margin: 20px 0;">
                        <p style="margin: 4px 0; color: #64748b;"><strong>Status:</strong> <span style="background-color: #d1fae5; color: #065f46; padding: 2px 8px; border-radius: 4px; font-size: 13px;">%s</span></p>
                        <p style="margin: 4px 0; color: #64748b;"><strong>Order Date:</strong> %s</p>
                    </div>
                    %s
                    <div style="text-align: right; margin-top: 20px; font-size: 18px; color: #1e293b;">
                        <strong>Total Amount: </strong><span style="color: #4f46e5; font-size: 22px; font-weight: bold;">$%s</span>
                    </div>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">We will notify you when your items are shipped. Thanks for shopping with us!</p>
                </div>
                """.formatted(order.getId(), order.getStatus(), order.getCreatedAt() != null ? order.getCreatedAt().toString() : "Recent", itemsTable.toString(), order.getTotalAmount());

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async("emailTaskExecutor")
    @Override
    public void sendOrderStatusUpdateEmail(String toEmail, Long orderId, OrderStatus status) {
        String subject = "Update on Order #" + orderId + " - Status: " + status;
        String statusColor = switch (status) {
            case CONFIRMED -> "#0284c7";
            case SHIPPED -> "#7c3aed";
            case DELIVERED -> "#059669";
            case CANCELLED -> "#dc2626";
            default -> "#4b5563";
        };

        String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <h2 style="color: #1e293b; margin-top: 0;">Order Status Update</h2>
                    <p style="color: #475569; font-size: 15px;">Your order <strong>#%d</strong> status has been updated:</p>
                    <div style="background-color: #f8fafc; padding: 18px; border-radius: 8px; text-align: center; margin: 20px 0; border: 1px solid #e2e8f0;">
                        <span style="font-size: 20px; font-weight: bold; color: %s;">%s</span>
                    </div>
                    <p style="color: #64748b; font-size: 14px;">You can view and track your order details anytime in your customer order history.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 12px; text-align: center;">© E-Commerce Inc. All rights reserved.</p>
                </div>
                """.formatted(orderId, statusColor, status.name());

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        if (mailSender == null) {
            logger.info("[MOCK MAIL DISPATCH] To: {} | Subject: '{}'", toEmail, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("Email successfully sent to {} with subject '{}'", toEmail, subject);
        } catch (Exception ex) {
            logger.warn("Could not dispatch SMTP email to {} with subject '{}' (Fallback logged). Reason: {}",
                    toEmail, subject, ex.getMessage());
        }
    }
}
