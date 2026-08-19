package com.Project1.project.service.impl;

import com.Project1.project.entity.Order;
import com.Project1.project.entity.OrderStatus;
import com.Project1.project.entity.User;
import com.Project1.project.exception.InvalidOrderStatusTransitionException;
import com.Project1.project.repository.CartRepository;
import com.Project1.project.repository.OrderRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.security.CurrentUserProvider;
import com.Project1.project.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private EmailService emailService;
    @InjectMocks private OrderServiceImpl orderService;

    @Test
    void updateOrderStatus_allowsValidTransitionsAndSendsEmail() {
        User customer = new User();
        customer.setEmail("customer@example.com");

        Order order = new Order();
        order.setId(1L);
        order.setUser(customer);
        order.setStatus(OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);
        orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);
        orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        verify(orderRepository, times(3)).save(order);
        verify(emailService).sendOrderStatusUpdateEmail("customer@example.com", 1L, OrderStatus.CONFIRMED);
        verify(emailService).sendOrderStatusUpdateEmail("customer@example.com", 1L, OrderStatus.SHIPPED);
        verify(emailService).sendOrderStatusUpdateEmail("customer@example.com", 1L, OrderStatus.DELIVERED);
    }

    @Test
    void updateOrderStatus_allowsCancellationOnlyBeforeShipping() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_rejectsInvalidTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED));
        verify(orderRepository, never()).save(any());
        verify(emailService, never()).sendOrderStatusUpdateEmail(anyString(), anyLong(), any(OrderStatus.class));
    }
}
