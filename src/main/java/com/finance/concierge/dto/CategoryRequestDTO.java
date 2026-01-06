package com.finance.concierge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO for Creating/Updating Category")
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Name cannot exist 50 characters")
    @Schema(description = "Name of the category", example = "Food")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(description = "Description of the category", example = "Expenses related to food and dining")
    private String description;

    @Size(max = 50, message = "Icon cannot exceed 50 characters")
    @Schema(description = "Icon identifier for the category", example = "fast-food")
    private String icon;

    @Size(max = 20, message = "Color code cannot exceed 20 characters")
    @Schema(description = "Color hex code for the category", example = "#FF5733")
    private String color;
}

