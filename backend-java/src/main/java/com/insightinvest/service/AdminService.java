package com.insightinvest.service;

import com.insightinvest.entity.User;
import com.insightinvest.entity.UserRole;
import com.insightinvest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Update user role
     */
    public User updateUserRole(Long userId, String newRole) {
        User user = getUserById(userId);
        try {
            user.setRole(UserRole.valueOf(newRole.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + newRole);
        }
        return userRepository.save(user);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    /**
     * Get system statistics
     */
    public Map<String, Object> getSystemStats() {
        long totalUsers = userRepository.count();
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("systemStatus", "Running");
        stats.put("timestamp", java.time.LocalDateTime.now());
        stats.put("version", "3.0.0-java");
        
        return stats;
    }
}
