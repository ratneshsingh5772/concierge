package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.CurrencyRequestDTO;
import com.finance.concierge.dto.CurrencyResponseDTO;
import com.finance.concierge.exception.GlobalExceptionHandler;
import com.finance.concierge.exception.ResourceNotFoundException;
import com.finance.concierge.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private MockMvc mockMvc;

    private CurrencyRequestDTO currencyRequest;
    private CurrencyResponseDTO currencyResponse;
    private List<CurrencyResponseDTO> currencyList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        currencyRequest = CurrencyRequestDTO.builder()
                .code("USD")
                .name("US Dollar")
                .symbol("$")
                .build();

        currencyResponse = CurrencyResponseDTO.builder()
                .id(1L)
                .code("USD")
                .name("US Dollar")
                .symbol("$")
                .build();

        currencyList = Arrays.asList(
                currencyResponse,
                CurrencyResponseDTO.builder()
                        .id(2L)
                        .code("EUR")
                        .name("Euro")
                        .symbol("€")
                        .build()
        );
    }

    @Test
    void getAllCurrencies_Success() throws Exception {
        // Given
        when(currencyService.getAllCurrencies()).thenReturn(currencyList);

        // When & Then
        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Currencies fetched successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("USD"))
                .andExpect(jsonPath("$.data[1].code").value("EUR"));

        verify(currencyService).getAllCurrencies();
    }

    @Test
    void getAllCurrencies_EmptyList() throws Exception {
        // Given
        when(currencyService.getAllCurrencies()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(currencyService).getAllCurrencies();
    }

    @Test
    void getCurrencyById_Success() throws Exception {
        // Given
        when(currencyService.getCurrencyById(1L)).thenReturn(currencyResponse);

        // When & Then
        mockMvc.perform(get("/api/currencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Currency fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("USD"));

        verify(currencyService).getCurrencyById(1L);
    }

    @Test
    void getCurrencyById_NotFound() throws Exception {
        // Given
        when(currencyService.getCurrencyById(999L))
                .thenThrow(new ResourceNotFoundException("Currency not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/currencies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Currency not found with id: 999"));

        verify(currencyService).getCurrencyById(999L);
    }

    @Test
    void createCurrency_Success() throws Exception {
        // Given
        when(currencyService.createCurrency(any(CurrencyRequestDTO.class))).thenReturn(currencyResponse);

        // When & Then
        mockMvc.perform(post("/api/currencies")
                .contentType("application/json")
                .content("""
                    {
                        "code": "USD",
                        "name": "US Dollar",
                        "symbol": "$"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Currency created successfully"))
                .andExpect(jsonPath("$.data.code").value("USD"));

        verify(currencyService).createCurrency(any(CurrencyRequestDTO.class));
    }

    @Test
    void createCurrency_InvalidInput() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/currencies")
                .contentType("application/json")
                .content("""
                    {
                        "code": "",
                        "name": "",
                        "symbol": ""
                    }
                    """))
                .andExpect(status().isBadRequest());

        verify(currencyService, never()).createCurrency(any(CurrencyRequestDTO.class));
    }

    @Test
    void updateCurrency_Success() throws Exception {
        // Given
        when(currencyService.updateCurrency(eq(1L), any(CurrencyRequestDTO.class))).thenReturn(currencyResponse);

        // When & Then
        mockMvc.perform(put("/api/currencies/1")
                .contentType("application/json")
                .content("""
                    {
                        "code": "USD",
                        "name": "US Dollar",
                        "symbol": "$"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Currency updated successfully"))
                .andExpect(jsonPath("$.data.code").value("USD"));

        verify(currencyService).updateCurrency(eq(1L), any(CurrencyRequestDTO.class));
    }

    @Test
    void updateCurrency_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/currencies/invalid")
                .contentType("application/json")
                .content("""
                    {
                        "code": "USD",
                        "name": "US Dollar",
                        "symbol": "$"
                    }
                    """))
                .andExpect(status().isBadRequest());

        verify(currencyService, never()).updateCurrency(any(Long.class), any(CurrencyRequestDTO.class));
    }

    @Test
    void deleteCurrency_Success() throws Exception {
        // Given
        doNothing().when(currencyService).deleteCurrency(1L);

        // When & Then
        mockMvc.perform(delete("/api/currencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Currency deleted successfully"));

        verify(currencyService).deleteCurrency(1L);
    }

    @Test
    void deleteCurrency_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Currency not found with id: 999")).when(currencyService).deleteCurrency(999L);

        // When & Then
        mockMvc.perform(delete("/api/currencies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Currency not found with id: 999"));

        verify(currencyService).deleteCurrency(999L);
    }

    // Unit tests using direct controller method calls
    @Test
    void getAllCurrencies_ControllerMethod_Success() {
        // Given
        when(currencyService.getAllCurrencies()).thenReturn(currencyList);

        // When
        ResponseEntity<ApiResponse<List<CurrencyResponseDTO>>> response = currencyController.getAllCurrencies();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Currencies fetched successfully", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().size());
        verify(currencyService).getAllCurrencies();
    }

    @Test
    void getCurrencyById_ControllerMethod_Success() {
        // Given
        when(currencyService.getCurrencyById(1L)).thenReturn(currencyResponse);

        // When
        ResponseEntity<ApiResponse<CurrencyResponseDTO>> response = currencyController.getCurrencyById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Currency fetched successfully", response.getBody().getMessage());
        assertEquals("USD", response.getBody().getData().getCode());
        verify(currencyService).getCurrencyById(1L);
    }

    @Test
    void createCurrency_ControllerMethod_Success() {
        // Given
        when(currencyService.createCurrency(any(CurrencyRequestDTO.class))).thenReturn(currencyResponse);

        // When
        ResponseEntity<ApiResponse<CurrencyResponseDTO>> response = currencyController.createCurrency(currencyRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Currency created successfully", response.getBody().getMessage());
        assertEquals(currencyResponse, response.getBody().getData());
        verify(currencyService).createCurrency(currencyRequest);
    }

    @Test
    void updateCurrency_ControllerMethod_Success() {
        // Given
        when(currencyService.updateCurrency(eq(1L), any(CurrencyRequestDTO.class))).thenReturn(currencyResponse);

        // When
        ResponseEntity<ApiResponse<CurrencyResponseDTO>> response = currencyController.updateCurrency(1L, currencyRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Currency updated successfully", response.getBody().getMessage());
        verify(currencyService).updateCurrency(1L, currencyRequest);
    }

    @Test
    void deleteCurrency_ControllerMethod_Success() {
        // Given
        doNothing().when(currencyService).deleteCurrency(1L);

        // When
        ResponseEntity<ApiResponse<Void>> response = currencyController.deleteCurrency(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Currency deleted successfully", response.getBody().getMessage());
        verify(currencyService).deleteCurrency(1L);
    }

    @Test
    void getAllCurrencies_ControllerMethod_EmptyList() {
        // Given
        when(currencyService.getAllCurrencies()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ApiResponse<List<CurrencyResponseDTO>>> response = currencyController.getAllCurrencies();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(0, response.getBody().getData().size());
    }

    @Test
    void createCurrency_ControllerMethod_ServiceException() {
        // Given
        when(currencyService.createCurrency(any(CurrencyRequestDTO.class)))
                .thenThrow(new RuntimeException("Duplicate currency code"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            currencyController.createCurrency(currencyRequest));

        assertEquals("Duplicate currency code", exception.getMessage());
        verify(currencyService).createCurrency(currencyRequest);
    }
}
