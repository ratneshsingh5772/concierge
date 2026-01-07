package com.finance.concierge.service;

import com.finance.concierge.dto.CategoryResponseDTO;
import java.util.List;

/**
 * Service interface for Category operations
 * Categories are predefined and available to all users
 */
public interface CategoryService {

    /**
     * Get all active categories (available to all users)
     */
    List<CategoryResponseDTO> getAllCategories();
}
