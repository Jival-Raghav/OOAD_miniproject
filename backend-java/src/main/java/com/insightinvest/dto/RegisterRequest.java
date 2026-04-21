package com.insightinvest.dto;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String riskProfile; // CONSERVATIVE, MODERATE, AGGRESSIVE

    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password, String riskProfile) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.riskProfile = riskProfile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRiskProfile() {
        return riskProfile;
    }

    public void setRiskProfile(String riskProfile) {
        this.riskProfile = riskProfile;
    }
}
