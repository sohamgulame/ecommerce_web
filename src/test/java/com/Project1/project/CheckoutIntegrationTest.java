package com.Project1.project;

import com.Project1.project.dto.request.AddToCartRequestDTO;
import com.Project1.project.dto.response.OrderResponseDTO;
import com.Project1.project.entity.Category;
import com.Project1.project.entity.Product;
import com.Project1.project.entity.Role;
import com.Project1.project.entity.User;
import com.Project1.project.repository.CartRepository;
import com.Project1.project.repository.CategoryRepository;
import com.Project1.project.repository.OrderRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.service.CartService;
import com.Project1.project.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CheckoutIntegrationTest {

    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private OrderRepository orderRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkout_createsOrderSnapshotsDecrementsStockAndClearsCart() {
        User user = new User();
        user.setName("Customer");
        user.setEmail("customer@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.ROLE_CUSTOMER);
        user = userRepository.save(user);

        Category category = categoryRepository.save(new Category("Testing", "Integration test category"));
        Product product = new Product();
        product.setName("Test product");
        product.setDescription("A product used in checkout tests");
        product.setPrice(new BigDecimal("24.50"));
        product.setStockQuantity(10);
        product.setCategory(category);
        product = productRepository.save(product);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of()));

        AddToCartRequestDTO addRequest = new AddToCartRequestDTO();
        addRequest.setProductId(product.getId());
        addRequest.setQuantity(3);
        cartService.addToCart(addRequest);

        OrderResponseDTO order = orderService.placeOrderFromCart();

        assertEquals("PLACED", order.getStatus());
        assertEquals(new BigDecimal("73.50"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals("Test product", order.getItems().get(0).getProductName());
        assertEquals(new BigDecimal("24.50"), order.getItems().get(0).getPrice());
        assertEquals(7, productRepository.findById(product.getId()).orElseThrow().getStockQuantity());
        assertTrue(cartService.getMyCart().getItems().isEmpty());
        assertEquals(1, orderRepository.count());
    }
}
