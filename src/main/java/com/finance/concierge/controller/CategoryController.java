package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.CategoryResponseDTO;
import com.finance.concierge.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category Management
 * Categories are predefined and available to all users (read-only)
 */
@Tag(name = "Categories", description = "Predefined expense categories (read-only)")
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all predefined categories
     * Categories are global and available to all users
     */
    @GetMapping
    @Operation(summary = "Get All Categories",
               description = "Returns all predefined expense categories available to all users")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
        log.info("Fetching all predefined categories");

        List<CategoryResponseDTO> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(ApiResponse.success(categories,
                "Retrieved " + (categories != null ? categories.size() : 0) + " categories"));
    }
}
