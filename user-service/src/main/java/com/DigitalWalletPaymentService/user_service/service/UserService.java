package com.DigitalWalletPaymentService.user_service.service;

import com.DigitalWalletPaymentService.user_service.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
}
