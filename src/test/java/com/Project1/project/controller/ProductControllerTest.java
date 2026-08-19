package com.Project1.project.controller;

import com.Project1.project.dto.response.ProductResponseDTO;
import com.Project1.project.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;

    @Test
    void getProduct_returnsPublicProductResponse() throws Exception {
        ProductResponseDTO response = new ProductResponseDTO(1L, "Keyboard", "Mechanical", new BigDecimal("99.99"),
                8, "Electronics", List.of(), 4.5);
        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Keyboard"))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void createProduct_rejectsInvalidRequestBeforeService() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":0,\"stockQuantity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void uploadProductImage_returnsUpdatedProduct() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "keyboard.jpg", "image/jpeg", "image-bytes".getBytes()
        );

        ProductResponseDTO updated = new ProductResponseDTO(1L, "Keyboard", "Mechanical",
                new BigDecimal("99.99"), 8, "Electronics",
                List.of("https://res.cloudinary.com/demo/image/upload/v1/products/keyboard.jpg"), 4.5);

        when(productService.uploadProductImage(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/products/1/images")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.imageUrls[0]").value("https://res.cloudinary.com/demo/image/upload/v1/products/keyboard.jpg"));
    }
}
