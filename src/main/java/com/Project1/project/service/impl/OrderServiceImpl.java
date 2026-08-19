package com.Project1.project.service.impl;

import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.*;
import com.Project1.project.exception.InvalidOrderStatusTransitionException;
import com.Project1.project.exception.OrderNotFoundException;
import com.Project1.project.mapper.OrderMapper;
import com.Project1.project.repository.*;
import com.Project1.project.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final com.Project1.project.security.CurrentUserProvider currentUserProvider;
    private final com.Project1.project.service.EmailService emailService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            com.Project1.project.security.CurrentUserProvider currentUserProvider,
                            com.Project1.project.service.EmailService emailService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.emailService = emailService;
    }

    private User resolveCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }

    @Override
    public OrderResponseDTO placeOrderFromCart() {
        User user = resolveCurrentUser();
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) throw new com.Project1.project.exception.EmptyCartException();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);

        // build items and compute total, but don't modify stock yet
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = productRepository.findById(ci.getProduct().getId()).orElseThrow(() -> new com.Project1.project.exception.ProductNotFoundException("Product not found: " + ci.getProduct().getId()));
            if (p.getStockQuantity() < ci.getQuantity()) {
                throw new com.Project1.project.exception.InsufficientStockException("Insufficient stock for product: " + p.getId());
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setProductNameSnapshot(p.getName());
            oi.setPriceSnapshot(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // decrement stock for each item atomically using a repository update; if any fail, throw and rollback
        for (OrderItem oi : saved.getItems()) {
            int updated = productRepository.decreaseStockIfAvailable(oi.getProductId(), oi.getQuantity());
            if (updated != 1) {
                throw new com.Project1.project.exception.InsufficientStockException("Insufficient stock for product during checkout: " + oi.getProductId());
            }
        }

        // clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        OrderResponseDTO responseDTO = OrderMapper.toResponse(saved);
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendOrderConfirmationEmail(user.getEmail(), responseDTO);
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        // enforce ownership: only the owner or admin may view
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!o.getUser().getId().equals(currentUserId)) {
            // check admin role
            User current = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
            if (current.getRole() != com.Project1.project.entity.Role.ROLE_ADMIN) {
                // avoid leaking existence
                throw new OrderNotFoundException("Order not found: " + id);
            }
        }
        return OrderMapper.toResponse(o);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getMyOrders(Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        List<OrderResponseDTO> dtos = page.stream().map(OrderMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable, OrderStatus status) {
        Page<Order> page;
        if (status != null) page = orderRepository.findByStatus(status, pageable);
        else page = orderRepository.findAll(pageable);
        List<OrderResponseDTO> dtos = page.stream().map(OrderMapper::toResponse).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    private boolean validTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return false;
        return switch (from) {
            case PLACED -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            default -> false;
        };
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        if (!validTransition(order.getStatus(), newStatus)) {
            throw new InvalidOrderStatusTransitionException("Invalid status transition: " + order.getStatus() + " -> " + newStatus);
        }

        // If transitioning to CANCELLED, return reserved stock back to inventory
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem oi : order.getItems()) {
                productRepository.restoreStock(oi.getProductId(), oi.getQuantity());
            }
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        if (saved.getUser() != null && saved.getUser().getEmail() != null && !saved.getUser().getEmail().isBlank()) {
            emailService.sendOrderStatusUpdateEmail(saved.getUser().getEmail(), saved.getId(), newStatus);
        }

        return OrderMapper.toResponse(saved);
    }
}
