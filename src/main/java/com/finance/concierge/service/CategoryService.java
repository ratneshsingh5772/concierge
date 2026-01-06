package com.finance.concierge.service;

import com.finance.concierge.dto.CategoryResponseDTO;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllCategories();
}

