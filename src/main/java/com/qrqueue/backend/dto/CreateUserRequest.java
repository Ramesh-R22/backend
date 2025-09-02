package com.qrqueue.backend.dto;

public class CreateUserRequest {
    private String username;
    private String email;
    private String role;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}