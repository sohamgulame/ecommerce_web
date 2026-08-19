package com.Project1.project.controller;

import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder() {
        OrderResponseDTO created = orderService.placeOrderFromCart();
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> myOrders(@PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDTO> page = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
