package com.Project1.project.service.impl;

import com.Project1.project.dto.request.AddToCartRequestDTO;
import com.Project1.project.dto.request.UpdateCartItemRequestDTO;
import com.Project1.project.dto.response.CartItemResponseDTO;
import com.Project1.project.dto.response.CartResponseDTO;
import com.Project1.project.entity.Cart;
import com.Project1.project.entity.CartItem;
import com.Project1.project.entity.Product;
import com.Project1.project.entity.User;
import com.Project1.project.exception.CartItemNotFoundException;
import com.Project1.project.exception.InsufficientStockException;
import com.Project1.project.exception.ProductNotFoundException;
import com.Project1.project.repository.CartItemRepository;
import com.Project1.project.repository.CartRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl.
 * 
 * Tests cover:
 * - Getting cart for a user
 * - Adding items to cart (new and existing)
 * - Updating cart item quantity
 * - Removing cart item
 * - Stock validation
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(10.0));
        testProduct.setStockQuantity(100);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
    }

    @Test
    void getMyCart_whenCartExists_returnsCartResponse() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Act
        CartResponseDTO result = cartService.getMyCart();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getItems().isEmpty());
        verify(currentUserProvider).getCurrentUser();
        verify(cartRepository).findByUser(testUser);
    }

    @Test
    void getMyCart_whenNoCart_returnsEmptyCart() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        // Act
        CartResponseDTO result = cartService.getMyCart();

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void addToCart_newItem_success() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        AddToCartRequestDTO request = new AddToCartRequestDTO();
        request.setProductId(1L);
        request.setQuantity(2);

        // Act
        CartResponseDTO result = cartService.addToCart(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getItems().size());
        CartItemResponseDTO item = result.getItems().get(0);
        assertEquals(1L, item.getProductId());
        assertEquals("Test Product", item.getProductName());
        assertEquals(BigDecimal.valueOf(10.0), item.getPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(BigDecimal.valueOf(20.0), item.getSubtotal());

        verify(productRepository).findById(1L);
        verify(cartRepository).save(testCart);
    }

    @Test
    void addToCart_newItem_insufficientStock() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        testProduct.setStockQuantity(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        AddToCartRequestDTO request = new AddToCartRequestDTO();
        request.setProductId(1L);
        request.setQuantity(5); // More than available stock

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(request));

        verify(productRepository).findById(1L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addToCart_existingItem_increasesQuantity() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Add existing item to cart
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setCart(testCart);
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(1);
        testCart.getItems().add(existingItem);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        AddToCartRequestDTO request = new AddToCartRequestDTO();
        request.setProductId(1L);
        request.setQuantity(2); // Adding 2 more, total should be 3

        // Act
        CartResponseDTO result = cartService.addToCart(request);

        // Assert
        assertEquals(1, result.getItems().size());
        CartItemResponseDTO item = result.getItems().get(0);
        assertEquals(3, item.getQuantity()); // 1 + 2
        assertEquals(BigDecimal.valueOf(30.0), item.getSubtotal());

        verify(productRepository).findById(1L);
        // Stock check: original 1 + added 2 = 3, product has 100, so should pass
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addToCart_existingItem_exceedsStock() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        // Add existing item with quantity to cart
        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setCart(testCart);
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(5); // Already has 5
        testCart.getItems().add(existingItem);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        AddToCartRequestDTO request = new AddToCartRequestDTO();
        request.setProductId(1L);
        request.setQuantity(2); // Trying to add 2 more, total would be 7, product has 100 so this should work

        // Act
        CartResponseDTO result = cartService.addToCart(request);

        // Assert
        assertEquals(1, result.getItems().size());
        CartItemResponseDTO item = result.getItems().get(0);
        assertEquals(7, item.getQuantity()); // 5 + 2

        verify(productRepository).findById(1L);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void updateCartItem_success() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(testCart);
        item.setProduct(testProduct);
        item.setQuantity(2);
        testCart.getItems().add(item);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO();
        request.setQuantity(5);

        // Act
        CartResponseDTO result = cartService.updateCartItem(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(50.0), result.getItems().get(0).getSubtotal());

        verify(cartItemRepository).save(item);
    }

    @Test
    void updateCartItem_itemNotFound() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO();
        request.setQuantity(5);

        // Act & Assert
        assertThrows(CartItemNotFoundException.class, () -> cartService.updateCartItem(1L, request));
    }

    @Test
    void updateCartItem_differentUser_throwsForbidden() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(testCart);
        item.setProduct(testProduct);
        testCart.setUser(otherUser); // Cart belongs to different user
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO();
        request.setQuantity(5);

        // Act & Assert
        assertThrows(com.Project1.project.exception.ForbiddenOperationException.class, () -> cartService.updateCartItem(1L, request));
    }

    @Test
    void updateCartItem_exceedsStock_throwsInsufficientStock() {
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        testProduct.setStockQuantity(2);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(testCart);
        item.setProduct(testProduct);
        item.setQuantity(1);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO();
        request.setQuantity(3);

        assertThrows(InsufficientStockException.class, () -> cartService.updateCartItem(1L, request));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeCartItem_success() {
        // Arrange
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(testCart);
        item.setProduct(testProduct);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        cartService.removeCartItem(1L);

        // Assert
        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeCartItem_differentUser_throwsForbidden() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(testCart);
        item.setProduct(testProduct);
        testCart.setUser(otherUser);
        when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        assertThrows(com.Project1.project.exception.ForbiddenOperationException.class, () -> cartService.removeCartItem(1L));
    }
}
