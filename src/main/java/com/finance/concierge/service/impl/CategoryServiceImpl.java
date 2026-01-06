package com.finance.concierge.service.impl;

import com.finance.concierge.dto.CategoryRequestDTO;
import com.finance.concierge.dto.CategoryResponseDTO;
import com.finance.concierge.entity.Category;
import com.finance.concierge.exception.ResourceNotFoundException;
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

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        Category category = Category.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .icon(requestDTO.getIcon())
                .color(requestDTO.getColor())
                .isActive(true)
                .build();
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO requestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setIcon(requestDTO.getIcon());
        category.setColor(requestDTO.getColor());

        Category updatedCategory = categoryRepository.save(category);
        return mapToDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setIsActive(false); // Soft delete
        categoryRepository.save(category);
    }
}
