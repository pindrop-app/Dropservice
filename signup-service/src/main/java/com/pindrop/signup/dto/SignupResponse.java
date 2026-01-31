package com.pindrop.signup.dto;

public class SignupResponse {
    private String userId;
    private String createdAt;

    public SignupResponse() {}

    public SignupResponse(String userId, String createdAt) {
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
