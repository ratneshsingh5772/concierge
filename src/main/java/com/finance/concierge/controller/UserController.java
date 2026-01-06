package com.finance.concierge.controller;

import com.finance.concierge.common.ApiResponse;
import com.finance.concierge.dto.UserCurrencyUpdateDTO;
import com.finance.concierge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user profile")
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/currency")
    @Operation(summary = "Update user preferred currency")
    public ResponseEntity<ApiResponse<Void>> updateUserCurrency(
            @PathVariable Long id,
            @Valid @RequestBody UserCurrencyUpdateDTO updateDTO) {
        userService.updateCurrency(id, updateDTO.getCurrencyCode());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User currency updated successfully")
                .build());
    }
}

