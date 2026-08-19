package com.Project1.project.service.impl;

import com.Project1.project.dto.response.OrderItemResponseDTO;
import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.OrderStatus;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, "noreply@ecommerce.local");
    }

    @Test
    void sendVerificationEmail_dispatchesMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendVerificationEmail("test@example.com", "123456");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_dispatchesMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail("user@example.com", "654321");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendOrderConfirmationEmail_dispatchesMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        OrderResponseDTO order = new OrderResponseDTO();
        order.setId(101L);
        order.setStatus("PLACED");
        order.setTotalAmount(new BigDecimal("199.99"));
        order.setCreatedAt(Instant.now());

        OrderItemResponseDTO item = new OrderItemResponseDTO();
        item.setProductName("Mechanical Keyboard");
        item.setQuantity(1);
        item.setPrice(new BigDecimal("199.99"));
        order.setItems(List.of(item));

        emailService.sendOrderConfirmationEmail("customer@example.com", order);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendOrderStatusUpdateEmail_dispatchesMimeMessage() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendOrderStatusUpdateEmail("customer@example.com", 101L, OrderStatus.SHIPPED);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_handlesMailExceptionGracefully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP connection failed")).when(mailSender).send(any(MimeMessage.class));

        // Should not throw exception to caller
        emailService.sendVerificationEmail("test@example.com", "123456");

        verify(mailSender).send(mimeMessage);
    }
}
