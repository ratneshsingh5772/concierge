package com.finance.concierge.service.impl;

import com.finance.concierge.dto.CurrencyRequestDTO;
import com.finance.concierge.dto.CurrencyResponseDTO;
import com.finance.concierge.entity.Currency;
import com.finance.concierge.exception.ResourceNotFoundException;
import com.finance.concierge.repository.CurrencyRepository;
import com.finance.concierge.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CurrencyResponseDTO createCurrency(CurrencyRequestDTO requestDTO) {
        if (currencyRepository.existsByCode(requestDTO.getCode())) {
            throw new IllegalArgumentException("Currency with code " + requestDTO.getCode() + " already exists");
        }
        Currency currency = Currency.builder()
                .code(requestDTO.getCode())
                .symbol(requestDTO.getSymbol())
                .name(requestDTO.getName())
                .country(requestDTO.getCountry())
                .build();
        Currency savedCurrency = currencyRepository.save(currency);
        return mapToDTO(savedCurrency);
    }

    @Override
    @Transactional
    public CurrencyResponseDTO updateCurrency(Long id, CurrencyRequestDTO requestDTO) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));

        currency.setCode(requestDTO.getCode());
        currency.setSymbol(requestDTO.getSymbol());
        currency.setName(requestDTO.getName());
        currency.setCountry(requestDTO.getCountry());

        Currency updatedCurrency = currencyRepository.save(currency);
        return mapToDTO(updatedCurrency);
    }

    @Override
    @Transactional
    public void deleteCurrency(Long id) {
        if (!currencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Currency not found with id: " + id);
        }
        currencyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyResponseDTO getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with id: " + id));
        return mapToDTO(currency);
    }

    private CurrencyResponseDTO mapToDTO(Currency currency) {
        return CurrencyResponseDTO.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .symbol(currency.getSymbol())
                .name(currency.getName())
                .country(currency.getCountry())
                .build();
    }
}

