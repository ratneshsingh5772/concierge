package com.finance.concierge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCurrencyUpdateDTO {
    @NotBlank
    @Size(min = 3, max = 3)
    private String currencyCode;
}

