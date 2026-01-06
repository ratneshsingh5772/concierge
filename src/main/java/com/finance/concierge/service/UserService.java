package com.finance.concierge.service;

import com.finance.concierge.entity.User;

public interface UserService {
    User updateCurrency(Long userId, String currencyCode);
    User getUserById(Long id);
}

