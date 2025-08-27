package com.qrqueue.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String jwt;
    private String role;
    private String username;

    public LoginResponse(String jwt, String role, String username) {
        this.jwt = jwt;
        this.role = role;
        this.username = username;
    }

    // getters
    public String getJwt() {
        return jwt;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }
}
