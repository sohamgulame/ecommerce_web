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
import com.Project1.project.exception.ProductNotFoundException;
import com.Project1.project.repository.CartItemRepository;
import com.Project1.project.repository.CartRepository;
import com.Project1.project.repository.ProductRepository;
import com.Project1.project.repository.UserRepository;
import com.Project1.project.service.CartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final com.Project1.project.security.CurrentUserProvider currentUserProvider;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository, com.Project1.project.security.CurrentUserProvider currentUserProvider) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    private User resolveCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }

    private Cart findOrCreateCartForUser(User user) {
        Optional<Cart> opt = cartRepository.findByUser(user);
        if (opt.isPresent()) return opt.get();
        Cart c = new Cart();
        c.setUser(user);
        return cartRepository.save(c);
    }

    private CartResponseDTO toCartResponse(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            BigDecimal price = p.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartItemResponseDTO(ci.getId(), p.getId(), p.getName(), price, ci.getQuantity(), subtotal);
        }).collect(Collectors.toList());
        BigDecimal total = items.stream().map(CartItemResponseDTO::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponseDTO(cart.getId(), items, total);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getMyCart() {
        User user = resolveCurrentUser();
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> new Cart());
        return toCartResponse(cart);
    }

    @Override
    public CartResponseDTO addToCart(AddToCartRequestDTO request) {
        User user = resolveCurrentUser();
        Cart cart = findOrCreateCartForUser(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

        // check if item exists
        Optional<CartItem> existing = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(product.getId())).findFirst();
        int newQuantity = request.getQuantity();
        if (existing.isPresent()) {
            CartItem item = existing.get();
            newQuantity = item.getQuantity() + request.getQuantity();
            // check stock
            if (product.getStockQuantity() < newQuantity) {
                throw new com.Project1.project.exception.InsufficientStockException("Insufficient stock for product: " + product.getId());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // check stock
            if (product.getStockQuantity() < newQuantity) {
                throw new com.Project1.project.exception.InsufficientStockException("Insufficient stock for product: " + product.getId());
            }
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQuantity);
            cart.getItems().add(item);
            // cascade will save via cartRepository save
            cartRepository.save(cart);
        }
        return toCartResponse(cart);
    }

    @Override
    public CartResponseDTO updateCartItem(Long itemId, UpdateCartItemRequestDTO request) {
        User user = resolveCurrentUser();
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new com.Project1.project.exception.ForbiddenOperationException("Not allowed");
        }
        if (request.getQuantity() == 0) {
            cartItemRepository.delete(item);
            // reload cart
            Cart cart = cartRepository.findByUser(user).orElse(new Cart());
            return toCartResponse(cart);
        }
        if (item.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new com.Project1.project.exception.InsufficientStockException(
                    "Insufficient stock for product: " + item.getProduct().getId());
        }
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        Cart cart = cartRepository.findByUser(user).orElse(new Cart());
        return toCartResponse(cart);
    }

    @Override
    public void removeCartItem(Long itemId) {
        User user = resolveCurrentUser();
        CartItem item = cartItemRepository.findById(itemId).orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new com.Project1.project.exception.ForbiddenOperationException("Not allowed");
        }
        cartItemRepository.delete(item);
    }
}
