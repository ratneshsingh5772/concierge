package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.BudgetRequestDTO;
import com.finance.concierge.dto.BudgetResponseDTO;
import com.finance.concierge.entity.User;
import com.finance.concierge.exception.ForbiddenException;
import com.finance.concierge.service.BudgetService;
import com.finance.concierge.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserBudgetController userBudgetController;

    private User authenticatedUser;
    private User targetUser;
    private BudgetRequestDTO budgetRequest;
    private BudgetResponseDTO budgetResponse;
    private List<BudgetResponseDTO> budgetList;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.USER)
                .build();

        targetUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.USER)
                .build();

        budgetRequest = BudgetRequestDTO.builder()
                .categoryName("Food")
                .budgetAmount(BigDecimal.valueOf(500.00))
                .budgetPeriod("MONTHLY")
                .alertThreshold(BigDecimal.valueOf(80.0))
                .isTotalBudget(false)
                .build();

        budgetResponse = BudgetResponseDTO.builder()
                .id(1L)
                .categoryName("Food")
                .budgetAmount(BigDecimal.valueOf(500.00))
                .currentSpending(BigDecimal.valueOf(450.75))
                .remaining(BigDecimal.valueOf(49.25))
                .percentageUsed(90.15)
                .budgetPeriod("MONTHLY")
                .alertThreshold(BigDecimal.valueOf(80.0))
                .isTotalBudget(false)
                .isOverBudget(false)
                .isNearLimit(true)
                .build();

        budgetList = Arrays.asList(
                budgetResponse,
                BudgetResponseDTO.builder()
                        .id(2L)
                        .categoryName("Transport")
                        .budgetAmount(BigDecimal.valueOf(300.00))
                        .currentSpending(BigDecimal.valueOf(150.00))
                        .remaining(BigDecimal.valueOf(150.00))
                        .percentageUsed(50.0)
                        .budgetPeriod("MONTHLY")
                        .isTotalBudget(false)
                        .isOverBudget(false)
                        .isNearLimit(false)
                        .build()
        );
    }


    @Test
    void setCategoryBudget_SelfAccess_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setBudget(1L, budgetRequest)).thenReturn(budgetResponse);

        // When & Then
        ResponseEntity<ApiResponse<BudgetResponseDTO>> response =
                userBudgetController.setCategoryBudget(1L, budgetRequest, authenticatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget set successfully for Food", response.getBody().getMessage());
        assertEquals("Food", response.getBody().getData().getCategoryName());

        verify(userService).getUserById(1L);
        verify(budgetService).setBudget(1L, budgetRequest);
    }

    @Test
    void setTotalBudget_SelfAccess_Success() throws Exception {
        // Given
        BudgetRequestDTO totalRequest = BudgetRequestDTO.builder()
                .budgetAmount(BigDecimal.valueOf(2000.00))
                .budgetPeriod("MONTHLY")
                .build();

        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setTotalBudget(1L, totalRequest)).thenReturn(budgetResponse);

        // When & Then
        ResponseEntity<ApiResponse<BudgetResponseDTO>> response =
                userBudgetController.setTotalBudget(1L, totalRequest, authenticatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Total budget set successfully", response.getBody().getMessage());

        verify(userService).getUserById(1L);
        verify(budgetService).setTotalBudget(1L, totalRequest);
    }

    @Test
    void updateBudget_SelfAccess_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setBudget(1L, budgetRequest)).thenReturn(budgetResponse);

        // When & Then
        ResponseEntity<ApiResponse<BudgetResponseDTO>> response =
                userBudgetController.updateBudget(1L, 1L, budgetRequest, authenticatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget updated successfully", response.getBody().getMessage());

        verify(userService).getUserById(1L);
        verify(budgetService).setBudget(1L, budgetRequest);
    }

    @Test
    void deleteBudget_SelfAccess_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        doNothing().when(budgetService).deleteBudget(1L, 1L);

        // When & Then
        ResponseEntity<ApiResponse<Void>> response =
                userBudgetController.deleteBudget(1L, 1L, authenticatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget deleted successfully", response.getBody().getMessage());

        verify(userService).getUserById(1L);
        verify(budgetService).deleteBudget(1L, 1L);
    }

    // Unit tests using direct controller method calls
    @Test
    void getAllBudgets_ControllerMethod_SelfAccess_Success() {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.getAllBudgets(1L, "MONTHLY")).thenReturn(budgetList);

        // When
        ResponseEntity<ApiResponse<List<BudgetResponseDTO>>> response =
                userBudgetController.getAllBudgets(1L, "MONTHLY", authenticatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Retrieved 2 budget(s)", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());
        verify(userService).getUserById(1L);
        verify(budgetService).getAllBudgets(1L, "MONTHLY");
    }

    @Test
    void setCategoryBudget_ControllerMethod_SelfAccess_Success() {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setBudget(1L, budgetRequest)).thenReturn(budgetResponse);

        // When
        ResponseEntity<ApiResponse<BudgetResponseDTO>> response =
                userBudgetController.setCategoryBudget(1L, budgetRequest, authenticatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget set successfully for Food", response.getBody().getMessage());
        verify(userService).getUserById(1L);
        verify(budgetService).setBudget(1L, budgetRequest);
    }

    @Test
    void setTotalBudget_ControllerMethod_SelfAccess_Success() {
        // Given
        BudgetRequestDTO totalRequest = BudgetRequestDTO.builder()
                .budgetAmount(BigDecimal.valueOf(2000.00))
                .budgetPeriod("MONTHLY")
                .build();

        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setTotalBudget(1L, totalRequest)).thenReturn(budgetResponse);

        // When
        ResponseEntity<ApiResponse<BudgetResponseDTO>> response =
                userBudgetController.setTotalBudget(1L, totalRequest, authenticatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Total budget set successfully", response.getBody().getMessage());
        verify(userService).getUserById(1L);
        verify(budgetService).setTotalBudget(1L, totalRequest);
    }

    @Test
    void updateBudget_ControllerMethod_SelfAccess_Success() {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setBudget(1L, budgetRequest)).thenReturn(budgetResponse);

        // When
        ResponseEntity<ApiResponse<BudgetResponseDTO>> response =
                userBudgetController.updateBudget(1L, 1L, budgetRequest, authenticatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget updated successfully", response.getBody().getMessage());
        verify(userService).getUserById(1L);
        verify(budgetService).setBudget(1L, budgetRequest);
    }

    @Test
    void deleteBudget_ControllerMethod_SelfAccess_Success() {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        doNothing().when(budgetService).deleteBudget(1L, 1L);

        // When
        ResponseEntity<ApiResponse<Void>> response =
                userBudgetController.deleteBudget(1L, 1L, authenticatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget deleted successfully", response.getBody().getMessage());
        verify(userService).getUserById(1L);
        verify(budgetService).deleteBudget(1L, 1L);
    }

    @Test
    void getAllBudgets_ControllerMethod_UserNotFound() {
        // Given - Use same userId to pass authorization check, then test user not found
        when(userService.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userBudgetController.getAllBudgets(1L, "MONTHLY", authenticatedUser));

        assertEquals("User not found", exception.getMessage());
        verify(userService).getUserById(1L);
        verify(budgetService, never()).getAllBudgets(anyLong(), anyString());
    }

    @Test
    void getAllBudgets_ControllerMethod_EmptyBudgets() {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.getAllBudgets(1L, "MONTHLY")).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ApiResponse<List<BudgetResponseDTO>>> response =
                userBudgetController.getAllBudgets(1L, "MONTHLY", authenticatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Retrieved 0 budget(s)", response.getBody().getMessage());
        assertEquals(0, response.getBody().getData().size());
    }

    @Test
    void setCategoryBudget_ControllerMethod_ServiceException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(targetUser);
        when(budgetService.setBudget(1L, budgetRequest))
                .thenThrow(new RuntimeException("Category not found"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userBudgetController.setCategoryBudget(1L, budgetRequest, authenticatedUser));

        assertEquals("Category not found", exception.getMessage());
        verify(userService).getUserById(1L);
        verify(budgetService).setBudget(1L, budgetRequest);
    }
}
