package com.Project1.project.service;

import com.Project1.project.dto.request.AddToCartRequestDTO;
import com.Project1.project.dto.request.UpdateCartItemRequestDTO;
import com.Project1.project.dto.response.CartResponseDTO;

public interface CartService {
    CartResponseDTO getMyCart();
    CartResponseDTO addToCart(AddToCartRequestDTO request);
    CartResponseDTO updateCartItem(Long itemId, UpdateCartItemRequestDTO request);
    void removeCartItem(Long itemId);
}
