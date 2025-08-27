package com.qrqueue.backend.payload;

public class LoginResponse {

    private final String token;
    private final String role;
    private final String username;

    public LoginResponse(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }
}
