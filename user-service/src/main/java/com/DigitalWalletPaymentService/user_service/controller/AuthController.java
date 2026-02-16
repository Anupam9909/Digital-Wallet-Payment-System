package com.DigitalWalletPaymentService.user_service.controller;

import com.DigitalWalletPaymentService.user_service.dto.JwtResponse;
import com.DigitalWalletPaymentService.user_service.dto.LoginRequest;
import com.DigitalWalletPaymentService.user_service.dto.SignupRequest;
import com.DigitalWalletPaymentService.user_service.entity.User;
import com.DigitalWalletPaymentService.user_service.repository.UserRepository;
import com.DigitalWalletPaymentService.user_service.service.UserService;
import com.DigitalWalletPaymentService.user_service.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final UserService userService;
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request){
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("⚠️ User already exists");
        }
        // map req -> userEntity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole("USER");  // Normal users only!
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // create new user --> wallet
        User savedUser = userService.createUser(user);
        return ResponseEntity.ok("✅ User registered successfully with Id: "+ savedUser.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("❌ User not found");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("❌ Invalid credentials");
        }

        // Generate token with claims
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(new JwtResponse(token));
    }

}
