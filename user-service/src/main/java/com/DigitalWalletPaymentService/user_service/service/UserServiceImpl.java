package com.DigitalWalletPaymentService.user_service.service;

import com.DigitalWalletPaymentService.user_service.client.WalletClient;
import com.DigitalWalletPaymentService.user_service.dto.CreateWalletRequest;
import com.DigitalWalletPaymentService.user_service.dto.WalletResponse;
import com.DigitalWalletPaymentService.user_service.entity.User;
import com.DigitalWalletPaymentService.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletClient walletClient;

    @Override
    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        try {
            CreateWalletRequest walletRequest = new CreateWalletRequest();
            walletRequest.setUserId(savedUser.getId());
            walletRequest.setCurrency("INR");

            WalletResponse walletResponse = walletClient.createWallet(walletRequest);
            System.out.println("✅ Wallet created successfully: " + walletResponse);

        } catch (Exception ex) {
            userRepository.deleteById(savedUser.getId());
            throw new RuntimeException("wallet Creation failed , User Rolled Backed",ex);
        }
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
