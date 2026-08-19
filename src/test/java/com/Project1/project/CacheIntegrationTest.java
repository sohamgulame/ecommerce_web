package com.Project1.project;

import com.Project1.project.dto.request.CategoryRequestDTO;
import com.Project1.project.dto.response.CategoryResponseDTO;
import com.Project1.project.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CacheIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Test
    void getCategoryById_cachesResult_andEvictsOnUpdate() {
        CategoryRequestDTO createReq = new CategoryRequestDTO();
        createReq.setName("Cache Test Category");
        createReq.setDescription("Testing cache behavior");
        CategoryResponseDTO created = categoryService.createCategory(createReq);

        Cache categoryCache = cacheManager.getCache("category");
        assertNotNull(categoryCache);
        assertNull(categoryCache.get(created.getId()));

        // First read: should populate cache
        CategoryResponseDTO read1 = categoryService.getCategoryById(created.getId());
        assertNotNull(read1);
        assertNotNull(categoryCache.get(created.getId()));

        // Update category: should evict cache
        CategoryRequestDTO updateReq = new CategoryRequestDTO();
        updateReq.setName("Updated Cache Category");
        updateReq.setDescription("Updated desc");
        categoryService.updateCategory(created.getId(), updateReq);

        assertNull(categoryCache.get(created.getId()), "Cache should be evicted after update");
    }
}
