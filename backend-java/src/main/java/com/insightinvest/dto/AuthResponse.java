package com.insightinvest.dto;

import java.util.Map;

public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private Map<String, Object> userInfo;

    public AuthResponse() {}

    public AuthResponse(String token, String username, String role, Map<String, Object> userInfo) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Map<String, Object> userInfo) {
        this.userInfo = userInfo;
    }
}
