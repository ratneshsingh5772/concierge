package com.finance.concierge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for category response
 * Categories are predefined and available to all users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category response DTO")
public class CategoryResponseDTO {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Food")
    private String name;

    @Schema(description = "Category description", example = "Food and beverage expenses")
    private String description;

    @Schema(description = "Category icon", example = "🍔")
    private String icon;

    @Schema(description = "Category color", example = "#FF6B6B")
    private String color;
}

