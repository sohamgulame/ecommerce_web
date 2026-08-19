package com.Project1.project.mapper;

import com.Project1.project.dto.response.OrderItemResponseDTO;
import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.Order;
import com.Project1.project.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponseDTO toResponse(Order order) {
        if (order == null) return null;
        List<OrderItemResponseDTO> items = order.getItems().stream().map(OrderMapper::toItemResponse).collect(Collectors.toList());
        return new OrderResponseDTO(order.getId(), order.getStatus().name(), order.getTotalAmount(), order.getCreatedAt(), items);
    }

    private static OrderItemResponseDTO toItemResponse(OrderItem item) {
        return new OrderItemResponseDTO(item.getId(), item.getProductId(), item.getProductNameSnapshot(), item.getPriceSnapshot(), item.getQuantity());
    }
}
