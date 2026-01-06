package com.finance.concierge.service;

import com.finance.concierge.dto.CurrencyRequestDTO;
import com.finance.concierge.dto.CurrencyResponseDTO;
import java.util.List;

public interface CurrencyService {
    List<CurrencyResponseDTO> getAllCurrencies();
    CurrencyResponseDTO createCurrency(CurrencyRequestDTO requestDTO);
    CurrencyResponseDTO updateCurrency(Long id, CurrencyRequestDTO requestDTO);
    void deleteCurrency(Long id);
    CurrencyResponseDTO getCurrencyById(Long id);
}

