package com.finance.concierge.service.impl;

import com.finance.concierge.dto.CategoryResponseDTO;
import com.finance.concierge.entity.Category;
import com.finance.concierge.repository.CategoryRepository;
import com.finance.concierge.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(Category::getIsActive)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryResponseDTO mapToDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .build();
    }
}

