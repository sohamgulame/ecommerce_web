package com.Project1.project.service;

import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDTO placeOrderFromCart();
    OrderResponseDTO getOrderById(Long id);
    Page<OrderResponseDTO> getMyOrders(Pageable pageable);
    Page<OrderResponseDTO> getAllOrders(Pageable pageable, OrderStatus status);
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus);
}
