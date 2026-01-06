package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.CurrencyRequestDTO;
import com.finance.concierge.dto.CurrencyResponseDTO;
import com.finance.concierge.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Tag(name = "Currencies", description = "API for managing currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    @Operation(summary = "Get all currencies")
    public ResponseEntity<ApiResponse<List<CurrencyResponseDTO>>> getAllCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(ApiResponse.<List<CurrencyResponseDTO>>builder()
                .success(true)
                .message("Currencies fetched successfully")
                .data(currencies)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency by ID")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>> getCurrencyById(@PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(ApiResponse.<CurrencyResponseDTO>builder()
                .success(true)
                .message("Currency fetched successfully")
                .data(currency)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a new currency")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>> createCurrency(@Valid @RequestBody CurrencyRequestDTO requestDTO) {
        CurrencyResponseDTO createdCurrency = currencyService.createCurrency(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CurrencyResponseDTO>builder()
                        .success(true)
                        .message("Currency created successfully")
                        .data(createdCurrency)
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing currency")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequestDTO requestDTO) {
        CurrencyResponseDTO updatedCurrency = currencyService.updateCurrency(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.<CurrencyResponseDTO>builder()
                .success(true)
                .message("Currency updated successfully")
                .data(updatedCurrency)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a currency")
    public ResponseEntity<ApiResponse<Void>> deleteCurrency(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Currency deleted successfully")
                .build());
    }
}

