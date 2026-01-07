package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.CategoryResponseDTO;
import com.finance.concierge.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test cases for CategoryController
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryResponseDTO foodCategory;
    private CategoryResponseDTO transportCategory;
    private List<CategoryResponseDTO> mockCategories;

    @BeforeEach
    void setUp() {
        foodCategory = CategoryResponseDTO.builder()
                .id(1L)
                .name("Food")
                .description("Food and beverages")
                .icon("🍔")
                .color("#FF6B6B")
                .build();

        transportCategory = CategoryResponseDTO.builder()
                .id(2L)
                .name("Transport")
                .description("Transportation costs")
                .icon("🚗")
                .color("#4ECDC4")
                .build();

        mockCategories = Arrays.asList(foodCategory, transportCategory);
    }

    @Test
    void testGetAllCategories_Success() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(mockCategories);

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Retrieved 2 categories", apiResponse.getMessage()); // Dynamic message with count
        assertNotNull(apiResponse.getData());
        assertEquals(2, apiResponse.getData().size());

        // Verify data content
        assertEquals(foodCategory, apiResponse.getData().get(0));
        assertEquals(transportCategory, apiResponse.getData().get(1));

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_EmptyList() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Retrieved 0 categories", apiResponse.getMessage()); // Dynamic message with count
        assertNotNull(apiResponse.getData());
        assertTrue(apiResponse.getData().isEmpty());

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_PredefinedCategories() {
        // Arrange - Test all 8 predefined categories
        CategoryResponseDTO bills = CategoryResponseDTO.builder()
                .id(3L).name("Bills").description("Utility bills and rent")
                .icon("📄").color("#95E1D3").build();

        CategoryResponseDTO entertainment = CategoryResponseDTO.builder()
                .id(4L).name("Entertainment").description("Movies, games, leisure")
                .icon("🎬").color("#F38181").build();

        CategoryResponseDTO shopping = CategoryResponseDTO.builder()
                .id(5L).name("Shopping").description("Clothing, electronics")
                .icon("🛍️").color("#AA96DA").build();

        CategoryResponseDTO health = CategoryResponseDTO.builder()
                .id(6L).name("Health").description("Healthcare and fitness")
                .icon("💊").color("#FCBAD3").build();

        CategoryResponseDTO education = CategoryResponseDTO.builder()
                .id(7L).name("Education").description("Books, courses, tuition")
                .icon("📚").color("#FFFFD2").build();

        CategoryResponseDTO other = CategoryResponseDTO.builder()
                .id(8L).name("Other").description("Miscellaneous expenses")
                .icon("📦").color("#A8D8EA").build();

        List<CategoryResponseDTO> allCategories = Arrays.asList(
            foodCategory, transportCategory, bills, entertainment,
            shopping, health, education, other
        );

        when(categoryService.getAllCategories()).thenReturn(allCategories);

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Retrieved 8 categories", apiResponse.getMessage()); // Dynamic message with count
        assertNotNull(apiResponse.getData());
        assertEquals(8, apiResponse.getData().size());

        // Verify all predefined categories are present
        String[] expectedNames = {"Food", "Transport", "Bills", "Entertainment",
                                "Shopping", "Health", "Education", "Other"};

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], apiResponse.getData().get(i).getName());
        }

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_ServiceException() {
        // Arrange
        when(categoryService.getAllCategories()).thenThrow(new RuntimeException("Service error"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryController.getAllCategories();
        });

        // Assert
        assertEquals("Service error", exception.getMessage());
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_ResponseStructure() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(mockCategories);

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert - Verify complete response structure
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertNotNull(apiResponse.getMessage());
        assertNotNull(apiResponse.getData());
        assertNotNull(apiResponse.getTimestamp());

        // Verify data integrity
        List<CategoryResponseDTO> categories = apiResponse.getData();
        assertEquals(2, categories.size());

        // Verify first category structure
        CategoryResponseDTO firstCategory = categories.get(0);
        assertNotNull(firstCategory.getId());
        assertNotNull(firstCategory.getName());
        assertNotNull(firstCategory.getDescription());
        assertNotNull(firstCategory.getIcon());
        assertNotNull(firstCategory.getColor());

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_ReadOnlyEndpoint() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(mockCategories);

        // Act
        categoryController.getAllCategories();

        // Assert - Should only call getAllCategories, no other service methods
        verify(categoryService, times(1)).getAllCategories();
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    void testGetAllCategories_NullData() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(null);

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertNull(apiResponse.getData()); // Data can be null

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_LargeDataset() {
        // Arrange - Simulate large dataset
        List<CategoryResponseDTO> largeDataset = Arrays.asList(
            foodCategory, transportCategory, foodCategory, transportCategory,
            foodCategory, transportCategory, foodCategory, transportCategory
        );

        when(categoryService.getAllCategories()).thenReturn(largeDataset);

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(8, apiResponse.getData().size());

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_MessageFormat() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(mockCategories);

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertEquals("Retrieved 2 categories", apiResponse.getMessage()); // Dynamic message with count

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_SingleCategory() {
        // Arrange - Test with single category
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(foodCategory));

        // Act
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<List<CategoryResponseDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals(1, apiResponse.getData().size());
        assertEquals("Food", apiResponse.getData().get(0).getName());

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_GlobalAccess() {
        // Arrange - Categories should be accessible to all users (no user context)
        when(categoryService.getAllCategories()).thenReturn(mockCategories);

        // Act - Call without any user context (simulating global access)
        ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> response = categoryController.getAllCategories();

        // Assert - Should return categories regardless of user context
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());

        verify(categoryService, times(1)).getAllCategories();
    }
}

