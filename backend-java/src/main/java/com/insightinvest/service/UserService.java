package com.insightinvest.service;

import com.insightinvest.dto.AuthResponse;
import com.insightinvest.dto.LoginRequest;
import com.insightinvest.dto.RegisterRequest;
import com.insightinvest.entity.Investor;
import com.insightinvest.entity.User;
import com.insightinvest.entity.UserRole;
import com.insightinvest.repository.UserRepository;
import com.insightinvest.security.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create new investor
        Investor investor = new Investor();
        investor.setUsername(request.getUsername());
        investor.setEmail(request.getEmail());
        investor.setPassword(passwordEncoder.encode(request.getPassword()));
        investor.setRole(UserRole.INVESTOR);
        investor.setRiskProfile(request.getRiskProfile() != null ? request.getRiskProfile() : "MODERATE");

        User savedUser = userRepository.save(investor);

        // Generate token
        String token = jwtUtility.generateToken(savedUser.getUsername(), savedUser.getRole().toString());

        // Build response
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", savedUser.getUserId());
        userInfo.put("username", savedUser.getUsername());
        userInfo.put("email", savedUser.getEmail());
        if (investor.getRiskProfile() != null) {
            userInfo.put("riskProfile", investor.getRiskProfile());
        }

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole().toString(), userInfo);
    }

    /**
     * Login user
     */
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate token
        String token = jwtUtility.generateToken(user.getUsername(), user.getRole().toString());

        // Build response
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        if (user instanceof Investor investor) {
            userInfo.put("riskProfile", investor.getRiskProfile());
        }

        return new AuthResponse(token, user.getUsername(), user.getRole().toString(), userInfo);
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(Long userId, Map<String, Object> updateData) {
        User user = getUserById(userId);
        
        if (updateData.containsKey("email")) {
            user.setEmail((String) updateData.get("email"));
        }
        
        if (user instanceof Investor investor && updateData.containsKey("riskProfile")) {
            investor.setRiskProfile((String) updateData.get("riskProfile"));
        }

        return userRepository.save(user);
    }
}
