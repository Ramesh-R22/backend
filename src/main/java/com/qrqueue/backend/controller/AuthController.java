package com.qrqueue.backend.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qrqueue.backend.dto.ChangePasswordRequest;
import com.qrqueue.backend.dto.ForgotPasswordRequest;
import com.qrqueue.backend.dto.LoginRequest;
import com.qrqueue.backend.dto.LoginResponse;
import com.qrqueue.backend.dto.ResetPasswordRequest;
import com.qrqueue.backend.dto.SignupRequest;
import com.qrqueue.backend.model.User;
import com.qrqueue.backend.repository.UserRepository;
import com.qrqueue.backend.security.JwtUtil;
import com.qrqueue.backend.service.EmailService;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    // In-memory store for reset codes (for demo; use DB for production)
    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // Allow login with either username or email
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(request.getUsername());
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name(), user.getUsername());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        userRepository.save(newUser);
        return ResponseEntity.ok("User created successfully");
    }

    // Change password for admin
    @PostMapping("/admin/change-password")
    public ResponseEntity<?> changePasswordAdmin(@RequestBody ChangePasswordRequest req) {
        return handleChangePasswordForRole("ADMIN", req);
    }

    // Change password for staff
    @PostMapping("/staff/change-password")
    public ResponseEntity<?> changePasswordStaff(@RequestBody ChangePasswordRequest req) {
        return handleChangePasswordForRole("STAFF", req);
    }

        // Change password for customer
    @PostMapping("/customer/change-password")
    public ResponseEntity<?> changePasswordCustomer(@RequestBody ChangePasswordRequest req) {
        return handleChangePasswordForRole("CUSTOMER", req);
    }

    private ResponseEntity<?> handleChangePasswordForRole(String requiredRole, ChangePasswordRequest req) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getRole().name().equals(requiredRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password changed successfully");
    }

    // Forgot password (send reset code to email)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit code
        resetCodes.put(req.getEmail(), code);
        emailService.sendEmail(req.getEmail(), "Password Reset Code", "Your password reset code is: " + code);
        return ResponseEntity.ok("Reset code sent to email");
    }

    // Reset password (using code)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        String code = resetCodes.get(req.getEmail());
        if (code == null || !code.equals(req.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset code");
        }
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        resetCodes.remove(req.getEmail());
        return ResponseEntity.ok("Password reset successfully");
    }
}
