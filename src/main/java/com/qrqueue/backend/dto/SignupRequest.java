package com.qrqueue.backend.dto;

import com.qrqueue.backend.model.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String username;
    private String password;
    private String email;
    private Role role;
}
