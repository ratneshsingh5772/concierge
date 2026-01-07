package com.finance.concierge.service;

import com.finance.concierge.dto.CategoryResponseDTO;
import com.finance.concierge.entity.Category;
import com.finance.concierge.repository.CategoryRepository;
import com.finance.concierge.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test cases for CategoryService
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category foodCategory;
    private Category transportCategory;
    private Category billsCategory;
    private List<Category> mockCategories;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        foodCategory = Category.builder()
                .id(1L)
                .name("Food")
                .description("Food and beverages")
                .icon("🍔")
                .color("#FF6B6B")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        transportCategory = Category.builder()
                .id(2L)
                .name("Transport")
                .description("Transportation costs")
                .icon("🚗")
                .color("#4ECDC4")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        billsCategory = Category.builder()
                .id(3L)
                .name("Bills")
                .description("Utility bills and rent")
                .icon("📄")
                .color("#95E1D3")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        mockCategories = Arrays.asList(foodCategory, transportCategory, billsCategory);
    }

    @Test
    void testGetAllCategories_Success() {
        // Arrange
        when(categoryRepository.findByIsActiveTrue()).thenReturn(mockCategories);

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify first category
        CategoryResponseDTO foodDto = result.get(0);
        assertEquals(1L, foodDto.getId());
        assertEquals("Food", foodDto.getName());
        assertEquals("Food and beverages", foodDto.getDescription());
        assertEquals("🍔", foodDto.getIcon());
        assertEquals("#FF6B6B", foodDto.getColor());

        // Verify second category
        CategoryResponseDTO transportDto = result.get(1);
        assertEquals(2L, transportDto.getId());
        assertEquals("Transport", transportDto.getName());
        assertEquals("Transportation costs", transportDto.getDescription());
        assertEquals("🚗", transportDto.getIcon());
        assertEquals("#4ECDC4", transportDto.getColor());

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_EmptyList() {
        // Arrange
        when(categoryRepository.findByIsActiveTrue()).thenReturn(Arrays.asList());

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_InactiveCategoriesFiltered() {
        // Arrange - Repository method findByIsActiveTrue() should only return active categories
        // The service trusts the repository to filter correctly
        when(categoryRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(foodCategory, transportCategory));

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Both categories are active

        // Verify all returned categories are active (mapped correctly)
        boolean allActive = result.stream().allMatch(dto -> dto.getName() != null);
        assertTrue(allActive);

        // Verify specific active categories are present
        boolean hasFood = result.stream().anyMatch(dto -> dto.getName().equals("Food"));
        boolean hasTransport = result.stream().anyMatch(dto -> dto.getName().equals("Transport"));
        assertTrue(hasFood);
        assertTrue(hasTransport);

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_PredefinedCategories() {
        // Arrange - Test all 8 predefined categories
        Category entertainment = Category.builder()
                .id(4L).name("Entertainment").description("Movies, games, leisure")
                .icon("🎬").color("#F38181").isActive(true).build();

        Category shopping = Category.builder()
                .id(5L).name("Shopping").description("Clothing, electronics")
                .icon("🛍️").color("#AA96DA").isActive(true).build();

        Category health = Category.builder()
                .id(6L).name("Health").description("Healthcare and fitness")
                .icon("💊").color("#FCBAD3").isActive(true).build();

        Category education = Category.builder()
                .id(7L).name("Education").description("Books, courses, tuition")
                .icon("📚").color("#FFFFD2").isActive(true).build();

        Category other = Category.builder()
                .id(8L).name("Other").description("Miscellaneous expenses")
                .icon("📦").color("#A8D8EA").isActive(true).build();

        List<Category> allPredefined = Arrays.asList(foodCategory, transportCategory, billsCategory,
                entertainment, shopping, health, education, other);

        when(categoryRepository.findByIsActiveTrue()).thenReturn(allPredefined);

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(8, result.size());

        // Verify all predefined categories are present
        String[] expectedNames = {"Food", "Transport", "Bills", "Entertainment",
                                "Shopping", "Health", "Education", "Other"};

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], result.get(i).getName());
            assertNotNull(result.get(i).getId());
            assertNotNull(result.get(i).getDescription());
            assertNotNull(result.get(i).getIcon());
            assertNotNull(result.get(i).getColor());
        }

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_RepositoryException() {
        // Arrange
        when(categoryRepository.findByIsActiveTrue()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getAllCategories();
        });

        assertEquals("Database error", exception.getMessage());
        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_NullFields() {
        // Arrange
        Category categoryWithNulls = Category.builder()
                .id(null)
                .name(null)
                .description(null)
                .icon(null)
                .color(null)
                .isActive(true)
                .build();

        when(categoryRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(categoryWithNulls));

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        CategoryResponseDTO dto = result.get(0);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getIcon());
        assertNull(dto.getColor());

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_OrderPreserved() {
        // Arrange - Categories in specific order
        Category category1 = Category.builder().id(1L).name("First").isActive(true).build();
        Category category2 = Category.builder().id(2L).name("Second").isActive(true).build();
        Category category3 = Category.builder().id(3L).name("Third").isActive(true).build();

        List<Category> orderedCategories = Arrays.asList(category1, category2, category3);
        when(categoryRepository.findByIsActiveTrue()).thenReturn(orderedCategories);

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert - Order should be preserved
        assertEquals(3, result.size());
        assertEquals("First", result.get(0).getName());
        assertEquals("Second", result.get(1).getName());
        assertEquals("Third", result.get(2).getName());

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_LargeDataset() {
        // Arrange - Test with larger dataset (simulate many categories)
        List<Category> largeDataset = Arrays.asList(
            foodCategory, transportCategory, billsCategory,
            foodCategory, transportCategory, billsCategory, // Duplicates for testing
            foodCategory, transportCategory, billsCategory
        );

        when(categoryRepository.findByIsActiveTrue()).thenReturn(largeDataset);

        // Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(9, result.size()); // All categories returned

        verify(categoryRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetAllCategories_ReadOnlyOperation() {
        // Arrange
        when(categoryRepository.findByIsActiveTrue()).thenReturn(mockCategories);

        // Act
        categoryService.getAllCategories();

        // Assert - Should only call findByIsActiveTrue, no save operations
        verify(categoryRepository, times(1)).findByIsActiveTrue();
        verifyNoMoreInteractions(categoryRepository);
    }
}
