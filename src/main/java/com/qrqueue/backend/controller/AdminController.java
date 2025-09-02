package com.qrqueue.backend.controller;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.qrqueue.backend.dto.CounterDto;
import com.qrqueue.backend.dto.CreateUserRequest;
import com.qrqueue.backend.model.Counter;
import com.qrqueue.backend.model.Role;
import com.qrqueue.backend.model.User;
import com.qrqueue.backend.repository.CounterRepository;
import com.qrqueue.backend.repository.QueueEntryRepository;
import com.qrqueue.backend.repository.UserRepository;
import com.qrqueue.backend.service.EmailService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // ===== Counter Management =====

    @PostMapping("/counters")
    public Counter createCounter(@RequestBody Counter counter) {
        return counterRepository.save(counter);
    }

    @GetMapping("/counters")
    public List<CounterDto> getAllCounters() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("/admin/counters accessed by user: {} with roles: {}", auth.getName(), auth.getAuthorities());
        return counterRepository.findAll().stream().map(counter -> {
            int waiting = (int) queueEntryRepository.findByCounter(counter).stream().filter(q -> !q.isServed()).count();
            return new CounterDto(
                counter.getId(),
                counter.getName(),
                counter.getDailyLimit(),
                waiting,
                counter.getAssignedStaff()
            );
        }).collect(Collectors.toList());
    }

    @PutMapping("/counters/{id}/limit")
    public Counter updateCounterLimit(@PathVariable Long id, @RequestParam int dailyLimit) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Counter not found"));
        counter.setDailyLimit(dailyLimit);
        return counterRepository.save(counter);
    }

    @DeleteMapping("/counters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCounter(@PathVariable Long id) {
        counterRepository.deleteById(id);
    }

    // ===== Staff Management =====

    @GetMapping("/staff")
    public List<User> getAllStaff() {
        return userRepository.findByRole(Role.STAFF);
    }

    @PostMapping("/staff")
    public User addStaff(@RequestBody User staff) {
        staff.setRole(Role.STAFF);
        return userRepository.save(staff);
    }

    @DeleteMapping("/staff/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStaff(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    // Assign staff to counter
    @PutMapping("/counters/{id}/assign-staff")
    public Counter assignStaffToCounter(@PathVariable Long id, @RequestParam Long staffId) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Counter not found"));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        if (staff.getRole() != Role.STAFF) {
            throw new RuntimeException("User is not a staff member");
        }
        counter.setAssignedStaff(staff);
        return counterRepository.save(counter);
    }

    // ===== User Management (STAFF/ADMIN) =====

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        if (req.getEmail() == null || req.getEmail().isEmpty() ||
            req.getUsername() == null || req.getUsername().isEmpty() ||
            req.getRole() == null || req.getRole().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required fields");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role");
        }
        String generatedPassword = generateRandomPassword(10);
        User newUser = new User();
        newUser.setUsername(req.getUsername());
        newUser.setEmail(req.getEmail());
        newUser.setPassword(passwordEncoder.encode(generatedPassword));
        newUser.setRole(role); // STAFF or ADMIN
        userRepository.save(newUser);

        String subject = "Your QR Smart Queue Account";
        String body = String.format(
            "Dear %s,\n\nYour account has been created.\n\nUsername: %s\nPassword: %s\n\nLogin: https://frontend-mu-three-43.vercel.app\n\nBest regards,\nAdmin",
            req.getUsername(), req.getUsername(), generatedPassword
        );
        emailService.sendEmail(req.getEmail(), subject, body);

        return ResponseEntity.ok("User created and credentials sent to email.");
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Generates a random alphanumeric password of given length. */
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
