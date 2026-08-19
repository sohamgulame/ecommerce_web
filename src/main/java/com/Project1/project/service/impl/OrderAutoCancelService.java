package com.Project1.project.service.impl;

import com.Project1.project.entity.Order;
import com.Project1.project.entity.OrderItem;
import com.Project1.project.entity.OrderStatus;
import com.Project1.project.repository.OrderRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Periodically cancels orders that remain in PLACED status beyond
 * the configured threshold. Restores stock for each cancelled order
 * and notifies the customer via email.
 */
@Service
public class OrderAutoCancelService {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final long thresholdHours;

    public OrderAutoCancelService(OrderRepository orderRepository,
                                  ProductRepository productRepository,
                                  EmailService emailService,
                                  @Value("${app.order.auto-cancel.threshold-hours:24}") long thresholdHours) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
        this.thresholdHours = thresholdHours;
    }

    /**
     * Runs on the configured cron schedule (default: every 30 minutes).
     * Finds all PLACED orders older than the threshold and cancels them.
     */
    @Scheduled(cron = "${app.order.auto-cancel.cron:0 */30 * * * ?}")
    @Transactional
    public void autoCancelStaleOrders() {
        Instant cutoff = Instant.now().minus(thresholdHours, ChronoUnit.HOURS);
        List<Order> staleOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PLACED, cutoff);

        if (staleOrders.isEmpty()) {
            return;
        }

        log.info("Found {} stale PLACED order(s) older than {} hours — auto-cancelling", staleOrders.size(), thresholdHours);

        for (Order order : staleOrders) {
            try {
                cancelOrderAndRestoreStock(order);
            } catch (Exception e) {
                log.error("Failed to auto-cancel order #{}: {}", order.getId(), e.getMessage(), e);
            }
        }
    }

    private void cancelOrderAndRestoreStock(Order order) {
        // Update status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restore stock for each item
        for (OrderItem item : order.getItems()) {
            productRepository.restoreStock(item.getProductId(), item.getQuantity());
            log.debug("Restored {} units of stock for product #{} (order #{})",
                    item.getQuantity(), item.getProductId(), order.getId());
        }

        log.info("Auto-cancelled order #{} (placed at {}) and restored stock for {} item(s)",
                order.getId(), order.getCreatedAt(), order.getItems().size());

        // Notify customer
        if (order.getUser() != null && order.getUser().getEmail() != null && !order.getUser().getEmail().isBlank()) {
            emailService.sendOrderStatusUpdateEmail(order.getUser().getEmail(), order.getId(), OrderStatus.CANCELLED);
        }
    }
}
