package com.Project1.project.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class CartResponseDTO {
    private Long id;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalAmount;

    public CartResponseDTO() {}

    public CartResponseDTO(Long id, List<CartItemResponseDTO> items, BigDecimal totalAmount) {
        this.id = id; this.items = items; this.totalAmount = totalAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public List<CartItemResponseDTO> getItems() { return items; }
    public void setItems(List<CartItemResponseDTO> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
