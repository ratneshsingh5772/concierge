package com.finance.concierge.service.impl;

import com.finance.concierge.entity.User;
import com.finance.concierge.exception.ResourceNotFoundException;
import com.finance.concierge.repository.UserRepository;
import com.finance.concierge.repository.CurrencyRepository;
import com.finance.concierge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;

    @Override
    @Transactional
    public User updateCurrency(Long userId, String currencyCode) {
        if (!currencyRepository.existsByCode(currencyCode)) {
            throw new IllegalArgumentException("Invalid currency code: " + currencyCode);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setCurrencyCode(currencyCode);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
         return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

